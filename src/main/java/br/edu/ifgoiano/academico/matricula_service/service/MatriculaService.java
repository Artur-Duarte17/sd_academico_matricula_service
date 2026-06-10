package br.edu.ifgoiano.academico.matricula_service.service;

import br.edu.ifgoiano.academico.matricula_service.config.RabbitMQConfig;
import br.edu.ifgoiano.academico.matricula_service.model.Matricula;
import br.edu.ifgoiano.academico.matricula_service.model.StatusMatricula;
import br.edu.ifgoiano.academico.matricula_service.repository.MatriculaRepository;

import br.edu.ifgoiano.grpc.ReservaVagaRequest;
import br.edu.ifgoiano.grpc.ReservaVagaResponse;

import br.edu.ifgoiano.grpc.LiberaVagaRequest;
import br.edu.ifgoiano.grpc.LiberaVagaResponse;
import br.edu.ifgoiano.grpc.TurmaGrpcServiceGrpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import br.edu.ifgoiano.academico.matricula_service.client.AlunoClient;

import br.edu.ifgoiano.academico.matricula_service.service.exception.TurmaServiceIndisponivelException;
import io.grpc.StatusRuntimeException;
import br.edu.ifgoiano.academico.matricula_service.service.exception.AlunoServiceIndisponivelException;
import feign.FeignException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatriculaService {

    private static final Logger log = LoggerFactory.getLogger(MatriculaService.class);

    private final MatriculaRepository matriculaRepository;
    private final AlunoClient alunoClient;
    private final TurmaGrpcServiceGrpc.TurmaGrpcServiceBlockingStub turmaGrpcStub;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public MatriculaService(
            MatriculaRepository matriculaRepository,
            AlunoClient alunoClient,
            TurmaGrpcServiceGrpc.TurmaGrpcServiceBlockingStub turmaGrpcStub,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper) {
        this.matriculaRepository = matriculaRepository;
        this.alunoClient = alunoClient;
        this.turmaGrpcStub = turmaGrpcStub;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public Matricula criarMatricula(Long alunoId, Long turmaId) {
        // Verifica se o aluno existe; se não existir, lança uma exceção
        if (!consultarExistenciaAluno(alunoId)) {
            throw new IllegalStateException("Aluno informado não existe.");
        }

        // Apenas alunos ATIVOS podem se matricular
        if (!consultarAlunoAtivo(alunoId)) {
            throw new IllegalStateException("Aluno não está ATIVO e não pode se matricular.");
        }

        boolean jaExisteMatriculaAtiva = matriculaRepository.existsByAlunoIdAndTurmaIdAndStatus(
                alunoId,
                turmaId,
                StatusMatricula.ATIVA);

        if (jaExisteMatriculaAtiva) {
            throw new IllegalStateException("Aluno já possui matrícula ativa nesta turma.");
        }

        // Pede ao Turma Service para reservar uma vaga na turma via gRPC
        ReservaVagaResponse reserva = reservarVagaNaTurma(turmaId);

        // Interrompe a matrícula se não for possível reservar a vaga
        if (!reserva.getSucesso()) {
            throw new IllegalStateException("Não foi possível reservar vaga na turma: " + reserva.getMensagem());
        }

        // A vaga já foi reservada (commit no Turma Service). A partir daqui, se algo
        // falhar, precisamos COMPENSAR liberando a vaga para não deixá-la presa.
        Matricula salva;
        try {
            Matricula matricula = new Matricula(alunoId, turmaId);
            salva = matriculaRepository.save(matricula);
        } catch (RuntimeException exception) {
            log.error("Falha ao salvar a matrícula após reservar vaga na turma {}. "
                    + "Compensando: liberando a vaga reservada.", turmaId, exception);
            compensarLiberandoVaga(turmaId);
            throw exception;
        }

        // Publica o evento de domínio (best-effort: falha de mensageria não desfaz a matrícula)
        publicarEvento(
                RabbitMQConfig.ROUTING_KEY_MATRICULA_CRIADA,
                alunoId,
                turmaId,
                "MATRICULA_CRIADA",
                "Matrícula criada para o aluno " + alunoId + " na turma " + turmaId + ".");

        return salva;
    }

    public List<Matricula> listarTodas() {
        return matriculaRepository.findAll();
    }

    public List<Matricula> listarPorAluno(Long alunoId) {
        return matriculaRepository.findByAlunoId(alunoId);
    }

    public List<Matricula> listarPorTurma(Long turmaId) {
        return matriculaRepository.findByTurmaId(turmaId);
    }

    public Matricula cancelarMatricula(Long alunoId, Long turmaId) {

        Matricula matricula = matriculaRepository
                .findByAlunoIdAndTurmaIdAndStatus(
                        alunoId,
                        turmaId,
                        StatusMatricula.ATIVA)
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe matrícula ativa para este aluno nesta turma."));

        // Primeiro pede ao Turma Service para liberar a vaga
        LiberaVagaResponse liberacao = liberarVagaNaTurma(turmaId);

        // Interrompe o cancelamento se a vaga não for liberada
        if (!liberacao.getSucesso()) {
            throw new IllegalStateException(
                    "Não foi possível liberar vaga na turma: "
                            + liberacao.getMensagem());
        }

        // A vaga já foi liberada (commit no Turma Service). Se o save falhar, precisamos
        // COMPENSAR reservando a vaga novamente para não criar inconsistência.
        Matricula salva;
        try {
            matricula.cancelar();
            salva = matriculaRepository.save(matricula);
        } catch (RuntimeException exception) {
            log.error("Falha ao salvar o cancelamento após liberar vaga na turma {}. "
                    + "Compensando: reservando a vaga novamente.", turmaId, exception);
            compensarReservandoVaga(turmaId);
            throw exception;
        }

        publicarEvento(
                RabbitMQConfig.ROUTING_KEY_MATRICULA_CANCELADA,
                alunoId,
                turmaId,
                "MATRICULA_CANCELADA",
                "Matrícula cancelada para o aluno " + alunoId + " na turma " + turmaId + ".");

        return salva;
    }

    private boolean consultarExistenciaAluno(Long alunoId) {
        try {
            return alunoClient.alunoExiste(alunoId);
        } catch (FeignException exception) {
            log.error(
                    "Falha ao consultar o aluno {} no Aluno Service: {}",
                    alunoId,
                    exception.getMessage());
            throw new AlunoServiceIndisponivelException(
                    "Não foi possível consultar o Aluno Service no momento.",
                    exception);
        }
    }

    private boolean consultarAlunoAtivo(Long alunoId) {
        try {
            return alunoClient.alunoAtivo(alunoId);
        } catch (FeignException exception) {
            log.error(
                    "Falha ao verificar se o aluno {} está ativo no Aluno Service: {}",
                    alunoId,
                    exception.getMessage());
            throw new AlunoServiceIndisponivelException(
                    "Não foi possível consultar o Aluno Service no momento.",
                    exception);
        }
    }

    private ReservaVagaResponse reservarVagaNaTurma(Long turmaId) {
        try {
            return turmaGrpcStub.reservarVaga(
                    ReservaVagaRequest.newBuilder()
                            .setTurmaId(turmaId)
                            .build());
        } catch (StatusRuntimeException exception) {
            log.error(
                    "Falha ao reservar vaga na turma {}: {}",
                    turmaId,
                    exception.getStatus());
            throw new TurmaServiceIndisponivelException(
                    "Não foi possível acessar o Turma Service no momento.",
                    exception);
        }
    }

    private LiberaVagaResponse liberarVagaNaTurma(Long turmaId) {
        try {
            return turmaGrpcStub.liberarVaga(
                    LiberaVagaRequest.newBuilder()
                            .setTurmaId(turmaId)
                            .build());
        } catch (StatusRuntimeException exception) {
            log.error(
                    "Falha ao liberar vaga na turma {}: {}",
                    turmaId,
                    exception.getStatus());
            throw new TurmaServiceIndisponivelException(
                    "Não foi possível acessar o Turma Service no momento.",
                    exception);
        }
    }

    /**
     * Compensação (saga): libera uma vaga previamente reservada quando o restante
     * da operação de criação falhou. Best-effort — apenas registra em log se falhar.
     */
    private void compensarLiberandoVaga(Long turmaId) {
        try {
            turmaGrpcStub.liberarVaga(
                    LiberaVagaRequest.newBuilder().setTurmaId(turmaId).build());
        } catch (StatusRuntimeException exception) {
            log.error("Falha na compensação ao liberar vaga na turma {}: {}",
                    turmaId, exception.getStatus());
        }
    }

    /**
     * Compensação (saga): reserva novamente uma vaga previamente liberada quando o
     * cancelamento falhou após liberar a vaga. Best-effort.
     */
    private void compensarReservandoVaga(Long turmaId) {
        try {
            turmaGrpcStub.reservarVaga(
                    ReservaVagaRequest.newBuilder().setTurmaId(turmaId).build());
        } catch (StatusRuntimeException exception) {
            log.error("Falha na compensação ao reservar vaga na turma {}: {}",
                    turmaId, exception.getStatus());
        }
    }

    /**
     * Publica um evento de domínio no exchange "academico.events". A mensagem é um
     * JSON (texto) com os campos esperados pelos serviços de Notificação e Histórico:
     * alunoId, turmaId, tipo e descricao.
     *
     * Best-effort: uma indisponibilidade do RabbitMQ não deve desfazer a matrícula
     * já persistida; apenas registramos o erro em log.
     */
    private void publicarEvento(String routingKey, Long alunoId, Long turmaId, String tipo, String descricao) {
        try {
            Map<String, Object> evento = new LinkedHashMap<>();
            evento.put("alunoId", alunoId);
            evento.put("turmaId", turmaId);
            evento.put("tipo", tipo);
            evento.put("descricao", descricao);

            String mensagem = objectMapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_ACADEMICO,
                    routingKey,
                    mensagem);

            log.info("[MATRICULA-SERVICE] Evento publicado ({}): {}", routingKey, mensagem);
        } catch (Exception exception) {
            log.error("[MATRICULA-SERVICE] Falha ao publicar evento {} para aluno {} / turma {}: {}",
                    routingKey, alunoId, turmaId, exception.getMessage(), exception);
        }
    }
}
