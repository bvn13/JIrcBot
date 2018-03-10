package ru.bvn13.jircbot.listeners.advices;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bvn13 on 27.01.2018.
 */
public class AdviceEngine {
    private static final String urlAdvice = "http://fucking-great-advice.ru/api/random";

    public static String getAdvice() throws Exception {
        StringBuffer content = new StringBuffer();
        try {
            URL url = new URL(urlAdvice);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            con.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("не могу получить совет для тебя");
        }
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(content.toString());

            String advice = (String) json.get("text");
            advice = advice.trim();
            advice = "" + advice.substring(0, 1).toLowerCase() + advice.substring(1);
            return advice;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("почини бота, блеать!");
        }

    }

}
