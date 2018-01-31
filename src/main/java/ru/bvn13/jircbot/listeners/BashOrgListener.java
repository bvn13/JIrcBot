package ru.bvn13.jircbot.listeners;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by bvn13 on 26.01.2018.
 */
@Component
public class BashOrgListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?bash";
    private static final String USER_AGENT = "Mozilla/5.0";

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        try {
            this.sendNotice(event, getRandomBashQuote());
        } catch (Exception e) {
            event.respond("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
        }

    }

    private String getDataFromConnection(HttpURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "windows-1251"));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private String getRandomBashQuote() throws Exception {
        URL obj = new URL("http://bash.im/random");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);
        //con.setRequestProperty("X-Requested-With", "XMLHttpRequest");

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Не удалось получить цитату!");
        }
        String response = getDataFromConnection(con);

        Document doc = Jsoup.parse(response);
        Elements quotes = doc.select(".quote .text");

        if (quotes.size() == 0) {
            throw new Exception("Не получено ни одной цитаты!");
        }

        Element quote = quotes.get(0);

        return quote.text();

    }


}
