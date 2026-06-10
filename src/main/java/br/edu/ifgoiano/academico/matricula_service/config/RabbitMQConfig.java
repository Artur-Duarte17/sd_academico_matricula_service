package br.edu.ifgoiano.academico.matricula_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para o Matrícula Service (lado PRODUTOR).
 *
 * O Matrícula Service publica eventos de negócio no exchange "academico.events"
 * usando as routing keys "matricula.criada" e "matricula.cancelada".
 *
 * Os serviços de Notificação e Histórico declaram suas próprias filas e bindings
 * para esse exchange; aqui apenas garantimos que o exchange exista (o AmqpAdmin
 * declara este bean na inicialização), para que a publicação funcione mesmo que
 * os consumidores ainda não tenham subido.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ACADEMICO = "academico.events";

    public static final String ROUTING_KEY_MATRICULA_CRIADA = "matricula.criada";
    public static final String ROUTING_KEY_MATRICULA_CANCELADA = "matricula.cancelada";

    @Bean
    public TopicExchange academicoExchange() {
        // durable = true, autoDelete = false (mesmos parâmetros usados pelos consumidores)
        return new TopicExchange(EXCHANGE_ACADEMICO, true, false);
    }
}
