package br.edu.ifgoiano.academico.matricula_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Dados para criar ou cancelar uma matrícula")
public class MatriculaRequestDTO {

    @NotNull(message = "O id do aluno é obrigatório.")
    @Schema(description = "ID do aluno (deve existir e estar ATIVO)", example = "1")
    private Long alunoId;

    @NotNull(message = "O id da turma é obrigatório.")
    @Schema(description = "ID da turma (deve existir e ter vaga)", example = "1")
    private Long turmaId;
}
