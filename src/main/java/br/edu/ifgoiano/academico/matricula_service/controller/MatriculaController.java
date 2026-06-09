package br.edu.ifgoiano.academico.matricula_service.controller;

import br.edu.ifgoiano.academico.matricula_service.dto.MatriculaRequest;
import br.edu.ifgoiano.academico.matricula_service.model.Matricula;
import br.edu.ifgoiano.academico.matricula_service.service.MatriculaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    private static final Logger logger = LoggerFactory.getLogger(MatriculaController.class);

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    public ResponseEntity<?> criarMatricula(@RequestBody @Valid MatriculaRequest request) {
        logger.info("[MATRICULA-SERVICE] POST /matriculas - Aluno: {}, Turma: {}", request.alunoId(), request.turmaId());
        try {
            Matricula matricula = matriculaService.criarMatricula(request.alunoId(), request.turmaId());
            return ResponseEntity.status(HttpStatus.CREATED).body(matricula);
        } catch (IllegalStateException exception) {
            logger.warn("[MATRICULA-SERVICE] Falha ao criar matrícula: {}", exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Matricula>> listarTodas() {
        logger.info("[MATRICULA-SERVICE] GET /matriculas");
        return ResponseEntity.ok(matriculaService.listarTodas());
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Matricula>> listarPorAluno(@PathVariable Long alunoId) {
        logger.info("[MATRICULA-SERVICE] GET /matriculas/aluno/{}", alunoId);
        return ResponseEntity.ok(matriculaService.listarPorAluno(alunoId));
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<Matricula>> listarPorTurma(@PathVariable Long turmaId) {
        logger.info("[MATRICULA-SERVICE] GET /matriculas/turma/{}", turmaId);
        return ResponseEntity.ok(matriculaService.listarPorTurma(turmaId));
    }

    @PutMapping("/cancelar")
    public ResponseEntity<?> cancelarMatricula(@RequestBody @Valid MatriculaRequest request) {
        logger.info("[MATRICULA-SERVICE] PUT /matriculas/cancelar - Aluno: {}, Turma: {}", request.alunoId(), request.turmaId());
        try {
            Matricula matricula = matriculaService.cancelarMatricula(request.alunoId(), request.turmaId());
            return ResponseEntity.ok(matricula);
        } catch (IllegalStateException exception) {
            logger.warn("[MATRICULA-SERVICE] Falha ao cancelar matrícula: {}", exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}