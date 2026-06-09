package br.edu.ifgoiano.academico.matricula_service.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_NOTIFICACOES = "fila.notificacoes";

    @Bean
    public Queue filaNotificacoes() {
        return new Queue(FILA_NOTIFICACOES, true);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Declarables loggingDeclarables() {
        TopicExchange logsExchange = new TopicExchange("logs.academico", true, false);
        Queue logsQueue = new Queue("logs.all", true);
        Binding logsBinding = BindingBuilder.bind(logsQueue).to(logsExchange).with("logs.#");
        return new Declarables(logsExchange, logsQueue, logsBinding);
    }
}