package br.edu.ifgoiano.academico.matricula_service.dto;

import br.edu.ifgoiano.academico.matricula_service.model.StatusMatricula;
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
public class MatriculaResponseDTO {
    private Long id;
    private Long alunoId;
    private Long turmaId;
    private StatusMatricula status;
    private LocalDateTime dataMatricula;
    private LocalDateTime dataCancelamento;
}
