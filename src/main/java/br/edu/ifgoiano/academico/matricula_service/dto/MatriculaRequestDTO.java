package br.edu.ifgoiano.academico.matricula_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados de entrada para criar ou cancelar uma matrícula.
 */
@Getter
@Setter
@NoArgsConstructor
public class MatriculaRequestDTO {

    @NotNull(message = "O id do aluno é obrigatório.")
    private Long alunoId;

    @NotNull(message = "O id da turma é obrigatório.")
    private Long turmaId;
}
