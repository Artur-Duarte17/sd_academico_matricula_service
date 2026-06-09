package br.edu.ifgoiano.academico.matricula_service.messaging.consumer;

import br.edu.ifgoiano.academico.matricula_service.messaging.config.RabbitMQConfig;
import br.edu.ifgoiano.academico.matricula_service.messaging.dto.MensagemNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarNotificacao(MensagemNotificacao mensagem) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.FILA_NOTIFICACOES,
                mensagem
        );
        logger.info("[MATRICULA-SERVICE] Notificação enviada: {}", mensagem.getMensagem());
    }
}