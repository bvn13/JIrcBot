package ru.bvn13.jircbot.listeners;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlRetrieverListener extends ListenerAdapter {

    private static final String PATTERN = "(?i)(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?";
    //"(?i)^(?:(?:https?|ftp)://)(?:\\\\S+(?::\\\\S*)?@)?(?:(?!(?:10|127)(?:\\\\.\\\\d{1,3}){3})(?!(?:169\\\\.254|192\\\\.168)(?:\\\\.\\\\d{1,3}){2})(?!172\\\\.(?:1[6-9]|2\\\\d|3[0-1])(?:\\\\.\\\\d{1,3}){2})(?:[1-9]\\\\d?|1\\\\d\\\\d|2[01]\\\\d|22[0-3])(?:\\\\.(?:1?\\\\d{1,2}|2[0-4]\\\\d|25[0-5])){2}(?:\\\\.(?:[1-9]\\\\d?|1\\\\d\\\\d|2[0-4]\\\\d|25[0-4]))|(?:(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff0-9]-*)*[a-z\\\\u00a1-\\\\uffff0-9]+)*(?:\\\\.(?:[a-z\\\\u00a1-\\\\uffff]{2,}))\\\\.?)(?::\\\\d{2,5})?(?:[/?#]\\\\S*)?$";


    public static class UrlInfo {
        @Getter
        @Setter
        private String url = "";
        @Getter
        @Setter
        private String title = "";
        @Getter
        @Setter
        private String description = "";
        @Getter
        @Setter
        private Boolean error = false;
    }

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        List<String> foundUrls = this.findUrl(event.getMessage().trim());
        if (foundUrls.size() == 0) {
            return;
        }

        for (String url : foundUrls) {
            UrlInfo info = this.checkUrl(url);
            if (info.getError()) {
                event.respond(info.getDescription());
            } else {
                //event.respondWith(String.format("/notice %s %s", ((MessageEvent) event).getChannel().getName(), String.format("%s (%s)", info.getTitle(), info.getDescription())));
                event.getBot().sendIRC().notice(((MessageEvent) event).getChannel().getName(), String.format("%s (%s)", info.getTitle(), info.getDescription()));
            }
        }

    }


    private List<String> findUrl(String message) {
        List<String> result = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile(PATTERN);
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                result.add(matcher.group());
            }
        } catch (RuntimeException rte) {
            rte.printStackTrace();
            return result;
        }
        return result;
    }

    private UrlInfo checkUrl(String url) {
        UrlInfo info = new UrlInfo();
        info.setUrl(url);

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (Exception ex) {
            ex.printStackTrace();
            info.setError(true);
            info.setDescription("ERROR retrieving url: "+url);
            return info;
        }
        try {
            Element title = doc.head().select("title").first();
            info.setTitle(title.text());
        } catch (Exception ex) {
            ex.printStackTrace();
            //info.setError(false);
            info.setDescription("ERROR retrieving title: "+url);
        }
        try {
            Element descr = doc.head().select("meta[name=\"description\"]").first();
            if (descr != null && descr.attr("content") != null) {
                info.setDescription(descr.attr("content").trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return info;
    }

}
