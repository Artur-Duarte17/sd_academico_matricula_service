package br.edu.ifgoiano.academico.matricula_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente HTTP responsável pela comunicação com o aluno-service.
 *
 * O OpenFeign cria automaticamente a implementação desta interface.
 * O nome "aluno-service" deve ser igual ao spring.application.name
 * configurado no microsserviço de alunos.
 */

@FeignClient(name="aluno-service")
public interface AlunoClient{
     /**
     * Consulta o aluno-service para verificar se um aluno existe.
     *
     * A chamada corresponde a:
     * GET /alunos/{id}/existe
     *
     * @param alunoId identificador do aluno
     * @return true se o aluno existir; false caso contrário
     */
    @GetMapping("/alunos/{id}/existe")
    boolean alunoExiste(@PathVariable("id") Long alunoId);

    /**
     * Consulta o aluno-service para verificar se um aluno está ATIVO
     * (apenas alunos ATIVOS podem se matricular).
     *
     * A chamada corresponde a:
     * GET /alunos/{id}/ativo
     *
     * @param alunoId identificador do aluno
     * @return true se o aluno existir e estiver ATIVO; false caso contrário
     */
    @GetMapping("/alunos/{id}/ativo")
    boolean alunoAtivo(@PathVariable("id") Long alunoId);
}