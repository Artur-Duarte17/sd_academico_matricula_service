package br.edu.ifgoiano.academico.matricula_service.service;

import br.edu.ifgoiano.academico.matricula_service.model.Matricula;
import br.edu.ifgoiano.academico.matricula_service.model.StatusMatricula;
import br.edu.ifgoiano.academico.matricula_service.repository.MatriculaRepository;

import br.edu.ifgoiano.grpc.ReservaVagaRequest;
import br.edu.ifgoiano.grpc.ReservaVagaResponse;

import br.edu.ifgoiano.grpc.LiberaVagaRequest;
import br.edu.ifgoiano.grpc.LiberaVagaResponse;
import br.edu.ifgoiano.grpc.TurmaGrpcServiceGrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import br.edu.ifgoiano.academico.matricula_service.client.AlunoClient;

import br.edu.ifgoiano.academico.matricula_service.service.exception.TurmaServiceIndisponivelException;
import io.grpc.StatusRuntimeException;
import br.edu.ifgoiano.academico.matricula_service.service.exception.AlunoServiceIndisponivelException;
import feign.FeignException;

import java.util.List;

@Service
public class MatriculaService {

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
            throw new IllegalStateException("Aluno já possui matrícula ativa nesta turma.");
        }

        // Pede ao Turma Service para reservar uma vaga na turma via gRPC fazendo
        // verificações
        ReservaVagaResponse reserva = reservarVagaNaTurma(turmaId);

        // Interrompe a matrícula se não for possível reservar a vaga
        if (!reserva.getSucesso()) {
            throw new IllegalStateException("Não foi possível reservar vaga na turma: " + reserva.getMensagem());
        }

        // Cria e salva a matrícula depois que a vaga foi reservada
        Matricula matricula = new Matricula(alunoId, turmaId);

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

        // Só cancela a matrícula depois que a vaga foi liberada
        matricula.cancelar();

        return matriculaRepository.save(matricula);
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

    private ReservaVagaResponse reservarVagaNaTurma(Long turmaId) {

        try {
            // Tenta reservar uma vaga no Turma Service
            return turmaGrpcStub.reservarVaga(
                    ReservaVagaRequest.newBuilder()
                            .setTurmaId(turmaId)
                            .build());

        } catch (StatusRuntimeException exception) {

            // Registra o erro ocorrido
            log.error(
                    "Falha ao reservar vaga na turma {}: {}",
                    turmaId,
                    exception.getStatus());

            // Informa que o Turma Service não respondeu
            throw new TurmaServiceIndisponivelException(
                    "Não foi possível acessar o Turma Service no momento.",
                    exception);
        }
    }

    private LiberaVagaResponse liberarVagaNaTurma(Long turmaId) {

        try {
            // Tenta liberar uma vaga no Turma Service
            return turmaGrpcStub.liberarVaga(
                    LiberaVagaRequest.newBuilder()
                            .setTurmaId(turmaId)
                            .build());

        } catch (StatusRuntimeException exception) {

            // Registra o erro ocorrido
            log.error(
                    "Falha ao liberar vaga na turma {}: {}",
                    turmaId,
                    exception.getStatus());

            // Informa que o Turma Service não respondeu
            throw new TurmaServiceIndisponivelException(
                    "Não foi possível acessar o Turma Service no momento.",
                    exception);
        }
    }
}
