package com.legaoyi.iov.message.processor.mq;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.legaoyi.iov.message.processor.handler.MessageHandler;
import com.legaoyi.iov.message.processor.mq.UpstreamMqMessageHandler;
import com.legaoyi.iov.message.processor.util.ServerRuntimeContext;
import com.legaoyi.mq.MQMessageHandler;

@Component("mediaUpstreamMessageRabbitmqListener")
@RabbitListener(queues = "${elink.upstream.media.topic}")
@ConditionalOnProperty(name= {"elink.upstream.media.topic","spring.rabbitmq.host"})
public class MediaUpstreamMessageRabbitmqListener {

    private static final Logger logger = LoggerFactory.getLogger(MediaUpstreamMessageRabbitmqListener.class);

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Autowired
    @Qualifier("serverRuntimeContext")
    protected ServerRuntimeContext serverRuntimeContext;

    @Value("${elink.upstream.media.topic}")
    private String mediaUpstreamMessageQueue;

    @Value("${rabbitmq.message.durable}")
    private boolean durable = true;

    @RabbitHandler
    public void onMessage(byte[] bytes) {
        String json = null;
        try {
            json = new String(bytes, DEFAULT_CHARSET);
            if (logger.isInfoEnabled()) {
                logger.info(json);
            }
            mediaUpstreamMessageHandler().handle(json);
        } catch (Exception e) {
            logger.error("handle mq Message error,message={}", json, e);
        }
    }

    @RabbitHandler
    public void onMessage(String json) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(json);
            }
            mediaUpstreamMessageHandler().handle(json);
        } catch (Exception e) {
            logger.error("handle mq Message error,message={}", json, e);
        }
    }

    @Bean("mediaUpstreamMessageQueue")
    public Queue mediaUpstreamMessageQueue() {
        return new Queue(mediaUpstreamMessageQueue, durable);
    }

    @Bean("mediaUpstreamMessageHandler")
    public MQMessageHandler mediaUpstreamMessageHandler() {
        UpstreamMqMessageHandler handler = new UpstreamMqMessageHandler();
        List<MessageHandler> handlers = new ArrayList<MessageHandler>();
        handlers.add(ServerRuntimeContext.getBean("upstreamMessageHandler", MessageHandler.class));
        handler.setHandlers(handlers);
        return handler;
    }

}
