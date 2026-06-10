package br.edu.ifgoiano.academico.matricula_service.service.exception;

/**
 * Exceção usada quando não é possível acessar o Turma Service rpc.
 */
public class TurmaServiceIndisponivelException extends RuntimeException {

    public TurmaServiceIndisponivelException(
            String mensagem,
            Throwable causa) {

        super(mensagem, causa);
    }
}