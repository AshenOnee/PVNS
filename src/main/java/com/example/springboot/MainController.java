package com.example.springboot;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;


@Controller
public class MainController {

    Logger logger = Logger.getLogger(MainController.class);

    @Autowired
    RabbitTemplate template;

    @RequestMapping("/")
    @ResponseBody
    ModelAndView home() {
        return new ModelAndView("main");
    }

    @RequestMapping("/process/{message}")
    @ResponseBody
    String error(@PathVariable("message") String message) {
        logger.info(String.format("Emit '%s'",message));
        String response = (String) template.convertSendAndReceive("query-example-6",message);
        logger.info(String.format("Received on producer '%s'",response));
        return String.valueOf("returned from worker : " + response);
    }

    @RequestMapping("/getSummary")
    @ResponseBody
    public ModelAndView  getSummary(
            @RequestParam(value="text",required=true) String text,
            @RequestParam(value="summarizertype",required=true) String summarizertype,
            @RequestParam(value="compression",required=true) double compression
    ) {
        JSONObject json = new JSONObject();
        try {
            json.put("text", text);
            json.put("summarizertype", summarizertype);
            json.put("compression", compression);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        logger.info(String.format("Emit '%s'", json.toString()));
        String response = (String) template.convertSendAndReceive("query-example-6", json.toString());
        logger.info(String.format("Received on producer '%s'",response));

        ModelAndView mav = new ModelAndView("summary");

        try {
            JSONObject responseJson = new JSONObject(response);
            mav.addObject("summary", responseJson.get("summary"));
            mav.addObject("text", responseJson.get("text"));
            mav.addObject("summarizertype", responseJson.get("summarizertype"));
            mav.addObject("compression", responseJson.get("compression"));

        } catch (JSONException e) {
            e.printStackTrace();
        }



//        return String.valueOf("returned from worker : " + response);
        return mav;


////        String summary = "";
//        try {
//            ISummarizer summarizer = new StatisticSummarizer();
//
//            switch (summarizertype){
//                case "Statistic": {
//                    summarizer = new StatisticSummarizer();
//                    break;
//                }
//                case "TextRank": {
//                    summarizer = new TextRankSummarizer();
//                    break;
//                }
//                case "KMeans": {
//                    summarizer = new KMeansSummarizer();
//                    break;
//                }
//                case "LSA": {
//                    summarizer = new LSASummarizer();
//                    break;
//                }
//            }
//
////            summary = summarizer.getSummary(text, compression);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        ModelAndView mav = new ModelAndView("summary");
//        mav.addObject("summary", summary);
//        mav.addObject("text", text);
//        mav.addObject("summarizertype", summarizertype);
//        mav.addObject("compression", compression);
//        return mav;
    }

}