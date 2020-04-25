package com.rabbitmq;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class MainController {

    Logger logger = Logger.getLogger(MainController.class);

    @Autowired
    RabbitTemplate template;

    @RequestMapping("/")
    ModelAndView home() {
        return new ModelAndView("main");
    }

    @RequestMapping("/getSummary")
    @ResponseBody
    public ModelAndView  getSummary(
            @RequestParam(value="text",required=true) String text,
            @RequestParam(value="summarizertype",required=true) String summarizertype,
            @RequestParam(value="compression",required=true) double compression
    ) {
        JSONObject json = new JSONObject();
        ModelAndView mav = new ModelAndView("summary");
        try {
            json.put("text", text);
            json.put("summarizertype", summarizertype);
            json.put("compression", compression);

            logger.info(String.format("Emit '%s'", json.toString()));
            String response = (String) template.convertSendAndReceive("query-example-6", json.toString());
            logger.info(String.format("Received on producer '%s'",response));

            JSONObject responseJson = new JSONObject(response);
            mav.addObject("summary", responseJson.get("summary"));
            mav.addObject("text", responseJson.get("text"));
            mav.addObject("summarizertype", responseJson.get("summarizertype"));
            mav.addObject("compression", responseJson.get("compression"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mav;

    }

}