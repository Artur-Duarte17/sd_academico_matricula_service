package br.edu.ifgoiano.academico.matricula_service.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

        /**
         * Retorna erro 503 quando o Aluno Service não estiver disponível.
         */
        @ExceptionHandler(AlunoServiceIndisponivelException.class)
        public ResponseEntity<String> tratarAlunoServiceIndisponivel(
                        AlunoServiceIndisponivelException exception) {

                return ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(
                                                "Não foi possível verificar os dados do aluno porque "
                                                                + "o serviço responsável está temporariamente indisponível. "
                                                                + "Tente novamente em alguns instantes.");
        }

        /**
         * Retorna erro 503 quando o Turma Service não estiver disponível.
         */
        @ExceptionHandler(TurmaServiceIndisponivelException.class)
        public ResponseEntity<String> tratarTurmaServiceIndisponivel(
                        TurmaServiceIndisponivelException exception) {

                return ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(
                                                "Não foi possível verificar ou atualizar as vagas da turma porque "
                                                                + "o serviço responsável está temporariamente indisponível. "
                                                                + "Tente novamente em alguns instantes.");
        }
}