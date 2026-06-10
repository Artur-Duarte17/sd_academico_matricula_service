package br.edu.ifgoiano.academico.matricula_service.messaging.dto;

import java.io.Serializable;

public class MensagemNotificacao implements Serializable {

    private Long alunoId;

    private Long turmaId;

    private String tipo;

    private String mensagem;

    public MensagemNotificacao() {
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    @Override
    public String toString() {
        return "MensagemNotificacao{" +
                "alunoId=" + alunoId +
                ", turmaId=" + turmaId +
                ", tipo='" + tipo + '\'' +
                ", mensagem='" + mensagem + '\'' +
                '}';
    }
}
