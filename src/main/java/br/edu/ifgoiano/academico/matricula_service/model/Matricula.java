package br.edu.ifgoiano.academico.matricula_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private Long alunoId;

    @Column(name = "turma_id", nullable = false)
    private Long turmaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMatricula status;

    @Column(name = "data_matricula", nullable = false, updatable = false)
    private LocalDateTime dataMatricula;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

   
    public Matricula() {
    }

    public Matricula(Long alunoId, Long turmaId) {
        this.alunoId = alunoId;
        this.turmaId = turmaId;
    }

    @PrePersist
    public void prePersist() {
        if (this.dataMatricula == null) {
            this.dataMatricula = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = StatusMatricula.ATIVA;
        }
    }

    public void cancelar() {
        this.status = StatusMatricula.CANCELADA;
        this.dataCancelamento = LocalDateTime.now();
    }

    public void trancar() {
        this.status = StatusMatricula.TRANCADA;
    }

    public boolean estaAtiva() {
        return StatusMatricula.ATIVA.equals(this.status);
    }

    public Long getId() {
        return id;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    public LocalDateTime getDataMatricula() {
        return dataMatricula;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }
}