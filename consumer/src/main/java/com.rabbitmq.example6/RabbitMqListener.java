package com.rabbitmq.example6;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import summarizer.*;

import java.io.IOException;

@Component
public class RabbitMqListener {
    Logger logger = Logger.getLogger(RabbitMqListener.class);

    @RabbitListener(queues = "query-example-6")
    public String worker1(String message) throws InterruptedException {
        logger.info("Received on worker : " + message);

        try {
            JSONObject json = new JSONObject(message);

            String text = json.get("text").toString();
            String summarizertype = json.get("summarizertype").toString();
            double compression = Double.parseDouble(json.get("compression").toString());

            String summary = "";
            ISummarizer summarizer = new StatisticSummarizer();

            switch (summarizertype){
                case "Statistic": {
                    summarizer = new StatisticSummarizer();
                    break;
                }
                case "TextRank": {
                    summarizer = new TextRankSummarizer();
                    break;
                }
                case "KMeans": {
                    summarizer = new KMeansSummarizer();
                    break;
                }
                case "LSA": {
                    summarizer = new LSASummarizer();
                    break;
                }
            }

            summary = summarizer.getSummary(text, compression);

            json.put("summary", summary);

            return json.toString();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return "УПС";
    }
}