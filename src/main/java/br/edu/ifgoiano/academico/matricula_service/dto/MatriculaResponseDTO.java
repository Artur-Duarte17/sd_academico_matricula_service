package br.edu.ifgoiano.academico.matricula_service.dto;

import br.edu.ifgoiano.academico.matricula_service.model.StatusMatricula;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dados de saída ao expor uma matrícula pela API.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Representação de uma matrícula retornada pela API")
public class MatriculaResponseDTO {

    @Schema(description = "Identificador da matrícula", example = "1")
    private Long id;

    @Schema(description = "ID do aluno", example = "1")
    private Long alunoId;

    @Schema(description = "ID da turma", example = "1")
    private Long turmaId;

    @Schema(description = "Status da matrícula", example = "ATIVA")
    private StatusMatricula status;

    @Schema(description = "Data/hora da matrícula", example = "2026-06-10T14:30:00")
    private LocalDateTime dataMatricula;

    @Schema(description = "Data/hora do cancelamento (se houver)", example = "2026-06-15T09:00:00")
    private LocalDateTime dataCancelamento;
}
