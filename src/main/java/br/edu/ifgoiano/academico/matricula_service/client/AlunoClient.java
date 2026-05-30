package br.edu.ifgoiano.academico.matricula_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AlunoClient {

    private final RestClient restClient;

    public AlunoClient(@Value("${aluno.service.url}") String alunoServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(alunoServiceUrl)
                .build();
    }

    public boolean alunoExiste(Long alunoId) {
        try {
            Boolean existe = restClient
                    .get()
                    .uri("/alunos/{id}/existe", alunoId)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(existe);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Não foi possível validar o aluno no aluno-service.");
        }
    }
}