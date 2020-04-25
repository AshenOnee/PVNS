package com.rabbitmq;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@EnableRabbit
@Configuration
public class RabbitConfiguration {
    Logger logger = Logger.getLogger(RabbitConfiguration.class);

    @Value("${CLOUDAMQP_URL}")
    private String CLOUDAMQP_URL;

    @Bean
    public ConnectionFactory connectionFactory() {
        //получаем адрес AMQP у провайдера
        String uri = CLOUDAMQP_URL;
        if (uri == null) //значит мы запущены локально и нужно подключаться к локальному rabbitmq
            uri = "amqp://guest:guest@localhost";
        URI url = null;
        try
        {
            url = new URI(uri);
        } catch (URISyntaxException e)
        {
            e.printStackTrace(); //тут ошибка крайне маловероятна
        }

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(url.getHost());
        connectionFactory.setUsername(url.getUserInfo().split(":")[0]);
        connectionFactory.setPassword(url.getUserInfo().split(":")[1]);
        if (!url.getPath().equals(""))
            connectionFactory.setVirtualHost(url.getPath().replace("/", ""));
        connectionFactory.setConnectionTimeout(3000);
        connectionFactory.setRequestedHeartBeat(30);
        return connectionFactory;

//        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setDefaultReceiveQueue("query-example-6");
        rabbitTemplate.setReplyTimeout(60 * 1000);
        return rabbitTemplate;
    }

    @Bean
    public Queue myQueue() {
        return new Queue("query-example-6");
    }
}