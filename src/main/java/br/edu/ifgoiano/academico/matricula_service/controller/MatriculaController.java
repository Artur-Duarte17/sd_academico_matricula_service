package br.edu.ifgoiano.academico.matricula_service.controller;

import br.edu.ifgoiano.academico.matricula_service.dto.MatriculaRequestDTO;
import br.edu.ifgoiano.academico.matricula_service.dto.MatriculaResponseDTO;
import br.edu.ifgoiano.academico.matricula_service.model.Matricula;
import br.edu.ifgoiano.academico.matricula_service.service.MatriculaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
@Tag(name = "Matrículas", description = "Criação, cancelamento e consulta de matrículas")
public class MatriculaController {

    private static final Logger logger = LoggerFactory.getLogger(MatriculaController.class);

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    @Operation(summary = "Criar matrícula",
            description = "Matricula um aluno ATIVO em uma turma com vaga. Publica o evento matricula.criada.")
    public ResponseEntity<?> criarMatricula(@RequestBody @Valid MatriculaRequestDTO request) {
        logger.info("[MATRICULA-SERVICE] POST /matriculas - Aluno: {}, Turma: {}",
                request.getAlunoId(), request.getTurmaId());
        try {
            Matricula matricula = matriculaService.criarMatricula(
                    request.getAlunoId(),
                    request.getTurmaId());

            return ResponseEntity.status(HttpStatus.CREATED).body(paraResponse(matricula));
        } catch (IllegalStateException exception) {
            logger.warn("[MATRICULA-SERVICE] Falha ao criar matrícula: {}", exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Listar matrículas", description = "Retorna todas as matrículas.")
    public ResponseEntity<List<MatriculaResponseDTO>> listarTodas() {
        logger.info("[MATRICULA-SERVICE] GET /matriculas");
        return ResponseEntity.ok(paraResponse(matriculaService.listarTodas()));
    }

    @GetMapping("/aluno/{alunoId}")
    @Operation(summary = "Listar matrículas por aluno", description = "Retorna as matrículas de um aluno.")
    public ResponseEntity<List<MatriculaResponseDTO>> listarPorAluno(
            @Parameter(description = "ID do aluno", example = "1") @PathVariable Long alunoId) {
        logger.info("[MATRICULA-SERVICE] GET /matriculas/aluno/{}", alunoId);
        return ResponseEntity.ok(paraResponse(matriculaService.listarPorAluno(alunoId)));
    }

    @GetMapping("/turma/{turmaId}")
    @Operation(summary = "Listar matrículas por turma", description = "Retorna as matrículas de uma turma.")
    public ResponseEntity<List<MatriculaResponseDTO>> listarPorTurma(
            @Parameter(description = "ID da turma", example = "1") @PathVariable Long turmaId) {
        logger.info("[MATRICULA-SERVICE] GET /matriculas/turma/{}", turmaId);
        return ResponseEntity.ok(paraResponse(matriculaService.listarPorTurma(turmaId)));
    }

    @PutMapping("/cancelar")
    @Operation(summary = "Cancelar matrícula",
            description = "Cancela a matrícula ativa do aluno na turma, libera a vaga e publica o evento matricula.cancelada.")
    public ResponseEntity<?> cancelarMatricula(@RequestBody @Valid MatriculaRequestDTO request) {
        logger.info("[MATRICULA-SERVICE] PUT /matriculas/cancelar - Aluno: {}, Turma: {}",
                request.getAlunoId(), request.getTurmaId());
        try {
            Matricula matricula = matriculaService.cancelarMatricula(
                    request.getAlunoId(),
                    request.getTurmaId());

            return ResponseEntity.ok(paraResponse(matricula));
        } catch (IllegalStateException exception) {
            logger.warn("[MATRICULA-SERVICE] Falha ao cancelar matrícula: {}", exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    /**
     * Converte a entidade Matricula no DTO de resposta exposto pela API.
     */
    private MatriculaResponseDTO paraResponse(Matricula matricula) {
        MatriculaResponseDTO response = new MatriculaResponseDTO();
        response.setId(matricula.getId());
        response.setAlunoId(matricula.getAlunoId());
        response.setTurmaId(matricula.getTurmaId());
        response.setStatus(matricula.getStatus());
        response.setDataMatricula(matricula.getDataMatricula());
        response.setDataCancelamento(matricula.getDataCancelamento());
        return response;
    }

    private List<MatriculaResponseDTO> paraResponse(List<Matricula> matriculas) {
        return matriculas.stream().map(this::paraResponse).toList();
    }
}
