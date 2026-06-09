package br.edu.ifgoiano.academico.matricula_service.dto;

import jakarta.validation.constraints.NotNull;

public record MatriculaRequest(

        @NotNull(message = "O id do aluno é obrigatório.")
        Long alunoId,

        @NotNull(message = "O id da turma é obrigatório.")
        Long turmaId
) {

}