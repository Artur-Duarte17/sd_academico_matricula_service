package br.edu.ifgoiano.academico.matricula_service.service;

import br.edu.ifgoiano.academico.matricula_service.messaging.dto.MensagemNotificacao;
import br.edu.ifgoiano.academico.matricula_service.messaging.consumer.NotificationProducer;
import br.edu.ifgoiano.academico.matricula_service.model.Matricula;
import br.edu.ifgoiano.academico.matricula_service.model.StatusMatricula;
import br.edu.ifgoiano.academico.matricula_service.repository.MatriculaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


import br.edu.ifgoiano.grpc.ReservaVagaRequest;
import br.edu.ifgoiano.grpc.ReservaVagaResponse;

import br.edu.ifgoiano.grpc.LiberaVagaRequest;
import br.edu.ifgoiano.grpc.LiberaVagaResponse;
import br.edu.ifgoiano.grpc.TurmaGrpcServiceGrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import br.edu.ifgoiano.academico.matricula_service.client.AlunoClient;

import br.edu.ifgoiano.academico.matricula_service.service.exception.AlunoServiceIndisponivelException;
import feign.FeignException;

import java.util.List;

@Service
public class MatriculaService {

    private static final Logger logger = LoggerFactory.getLogger(MatriculaService.class);

    private final MatriculaRepository matriculaRepository;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationProducer notificationProducer;
    public MatriculaService(MatriculaRepository matriculaRepository,
                            RabbitTemplate rabbitTemplate,
                            NotificationProducer notificationProducer) {
        this.matriculaRepository = matriculaRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.notificationProducer = notificationProducer;
    private static final Logger log = LoggerFactory.getLogger(MatriculaService.class);

    private final MatriculaRepository matriculaRepository;
    private final AlunoClient alunoClient;
    private final TurmaGrpcServiceGrpc.TurmaGrpcServiceBlockingStub turmaGrpcStub;

    public MatriculaService(
            MatriculaRepository matriculaRepository,
            AlunoClient alunoClient,
            TurmaGrpcServiceGrpc.TurmaGrpcServiceBlockingStub turmaGrpcStub) {
        this.matriculaRepository = matriculaRepository;
        this.alunoClient = alunoClient;
        this.turmaGrpcStub = turmaGrpcStub;
    }


    public Matricula criarMatricula(Long alunoId, Long turmaId) {
        logger.info("[MATRICULA-SERVICE] Criando matrícula - Aluno: {}, Turma: {}", alunoId, turmaId);

        boolean jaExisteMatriculaAtiva =
                matriculaRepository.existsByAlunoIdAndTurmaIdAndStatus(
                        alunoId,
                        turmaId,
                        StatusMatricula.ATIVA
                );
        boolean alunoExiste = consultarExistenciaAluno(alunoId);
        // verifica se aluno existe, se não existir lança uma exceção
        if (!alunoExiste) {
            throw new IllegalStateException("Aluno informado não existe.");
        }

        boolean jaExisteMatriculaAtiva = matriculaRepository.existsByAlunoIdAndTurmaIdAndStatus(
                alunoId,
                turmaId,
                StatusMatricula.ATIVA);

        if (jaExisteMatriculaAtiva) {
            logger.warn("[MATRICULA-SERVICE] Aluno {} já possui matrícula ativa na turma {}", alunoId, turmaId);
            throw new IllegalStateException("Aluno já possui matrícula ativa nesta turma.");
        }

        MensagemNotificacao mensagem = new MensagemNotificacao();
        mensagem.setAlunoId(alunoId);
        mensagem.setTurmaId(turmaId);
        mensagem.setTipo("MATRICULA_CRIADA");
        mensagem.setMensagem("Matrícula realizada com sucesso.");

        rabbitTemplate.convertAndSend("fila.notificacoes", mensagem);

        // Pede ao Turma Service para reservar uma vaga na turma via gRPC
        ReservaVagaResponse reserva = turmaGrpcStub.reservarVaga(
                ReservaVagaRequest.newBuilder()
                        .setTurmaId(turmaId)
                        .build());

        // Interrompe a matrícula se não for possível reservar a vaga
        if (!reserva.getSucesso()) {
            throw new IllegalStateException("Não foi possível reservar vaga na turma: " + reserva.getMensagem());
        }

        // Cria e salva a matrícula depois que a vaga foi reservada
        Matricula matricula = new Matricula(alunoId, turmaId);
        Matricula salva = matriculaRepository.save(matricula);

        logger.info("[MATRICULA-SERVICE] Matrícula realizada com sucesso - ID: {}, Aluno: {}, Turma: {}",
                salva.getId(), alunoId, turmaId);

        return salva;
        return matriculaRepository.save(matricula);

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
        logger.info("[MATRICULA-SERVICE] Cancelando matrícula - Aluno: {}, Turma: {}", alunoId, turmaId);

        Matricula matricula = matriculaRepository
                .findByAlunoIdAndTurmaIdAndStatus(
                        alunoId,
                        turmaId,
                        StatusMatricula.ATIVA
                )
                .orElseThrow(() -> {
                    logger.warn("[MATRICULA-SERVICE] Nenhuma matrícula ativa encontrada - Aluno: {}, Turma: {}", alunoId, turmaId);
                    return new IllegalStateException("Não existe matrícula ativa para este aluno nesta turma.");
                });

        matricula.cancelar();
        Matricula cancelada = matriculaRepository.save(matricula);

        logger.info("[MATRICULA-SERVICE] Matrícula cancelada com sucesso - ID: {}, Aluno: {}, Turma: {}",
                cancelada.getId(), alunoId, turmaId);

        return cancelada;
                        StatusMatricula.ATIVA)
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe matrícula ativa para este aluno nesta turma."));

        matricula.cancelar();
        Matricula matriculaCancelada = matriculaRepository.save(matricula);

        // Libera a vaga na turma via gRPC após cancelar a matrícula
        LiberaVagaResponse liberacao = turmaGrpcStub.liberarVaga(
                LiberaVagaRequest.newBuilder()
                        .setTurmaId(turmaId)
                        .build());

        if (!liberacao.getSucesso()) {
            // Não reverte o cancelamento; apenas registra a inconsistência
            log.warn("Falha ao liberar vaga na turma {}: {}", turmaId, liberacao.getMensagem());
        }

        return matriculaCancelada;
    }

    private boolean consultarExistenciaAluno(Long alunoId) {

        try {
            // Pergunta ao Aluno Service se o aluno existe
            return alunoClient.alunoExiste(alunoId);

        } catch (FeignException exception) {

            // Registra o erro ocorrido durante a consulta
            log.error(
                    "Falha ao consultar o aluno {} no Aluno Service: {}",
                    alunoId,
                    exception.getMessage());

            // Informa que não foi possível acessar o Aluno Service
            throw new AlunoServiceIndisponivelException(
                    "Não foi possível consultar o Aluno Service no momento.",
                    exception);
        }

    }
}
