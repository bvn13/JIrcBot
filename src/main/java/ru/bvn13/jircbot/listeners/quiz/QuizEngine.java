package ru.bvn13.jircbot.listeners.quiz;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by bvn13 on 26.01.2018.
 */
public class QuizEngine {

    private static final String USER_AGENT = "Mozilla/5.0";

    private String getDataFromConnection(HttpsURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public Question getQuestion() throws Exception {
        URL obj = new URL("https://baza-otvetov.ru/quiz/ask");
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("X-Requested-With", "XMLHttpRequest");

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Не удалось получить очередной вопрос из базы!");
        }
        String response = getDataFromConnection(con);

        Question q = new Question();

        Document doc = Jsoup.parse(response.toString());
        Element question = doc.select("h3.q_id").first();
        q.setId(question.attr("id"));
        q.setQuestion(question.html());
        Elements answers = doc.select("table td h4");
        for (Element answer : answers) {
            q.getAnswers().add(answer.html());
        }

        if (q.getId().isEmpty() || q.getQuestion().isEmpty() || q.getAnswers().size() == 0) {
            throw new Exception("Получены не все данные о вопросе и ответах!");
        }

        // retrieve correct answer
        for (String a : q.getAnswers()) {
            if (checkAnswer(q.getId(), a)) {
                q.setCorrectAnswer(a);
            }
        }

        if (q.getCorrectAnswer() == null) {
            throw new Exception("Не получен правильный ответ!");
        }

        return q;
    }


    private boolean checkAnswer(String id, String answer) throws Exception {
        String data = "q_id="+id+"&answer="+ URLEncoder.encode(answer, "utf-8");
        URL obj = new URL("https://baza-otvetov.ru/quiz/check");
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setDoOutput(true);

        // optional default is GET
        con.setRequestMethod("POST");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("X-Requested-With", "XMLHttpRequest");

        try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write( data.getBytes() );
        }

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Не удалось проверить правильность ответов!");
        }
        String response = getDataFromConnection(con);
        if (response.isEmpty()) {
            throw new Exception("Не удалось проверить правильность ответов!");
        }

        return response.contains("Правильно!");
    }

}
