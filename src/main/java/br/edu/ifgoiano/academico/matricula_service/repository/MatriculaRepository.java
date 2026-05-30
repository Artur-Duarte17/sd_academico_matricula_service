package br.edu.ifgoiano.academico.matricula_service.repository;

import br.edu.ifgoiano.academico.matricula_service.model.Matricula;
import br.edu.ifgoiano.academico.matricula_service.model.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    Optional<Matricula> findByAlunoIdAndTurmaIdAndStatus(
            Long alunoId,
            Long turmaId,
            StatusMatricula status
    );

    boolean existsByAlunoIdAndTurmaIdAndStatus(
            Long alunoId,
            Long turmaId,
            StatusMatricula status
    );

    List<Matricula> findByAlunoId(Long alunoId);

    List<Matricula> findByTurmaId(Long turmaId);
}