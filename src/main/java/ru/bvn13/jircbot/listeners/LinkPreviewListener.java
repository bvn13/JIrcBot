package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

/**
 * Created by bvn13 on 23.01.2018.
 */
public class LinkPreviewListener extends ListenerAdapter {

    private static final Pattern REGEX = Pattern.compile("(?i)(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?");


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        List<String> links = findLink(event.getMessage());
        for (String link : links) {
            String info = parseLink(link);
            if (info != null && !info.isEmpty()) {
                event.getBot().sendIRC().notice(((MessageEvent) event).getChannel().getName(), info);
            }
        };

    }


    public List<String> findLink(String message) {
        Matcher matcher = REGEX.matcher(message);
        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group());
        }
        return links;
    }


    private String parseLink(String link) throws Exception {
        String content = retrieveContentByLink(link);

        String encoding = null; //getCharsetFromHeaders(content.toString());
        if (encoding == null) {
            encoding = getCharsetFromBody(content.toString());
        }

        String title = "";

        if (encoding != null && !encoding.isEmpty()) {
            content = retrieveContentByLinkWithEncoding(link, encoding);
        }

        title = content.substring(content.indexOf("<title>") + 7, content.indexOf("</title>"));

        return "Title: "+title.toString();
    }

    public String retrieveContentByLink(String link) {
        return retrieveContentByLinkWithEncoding(link, "UTF-8");
    }

    public String retrieveContentByLinkWithEncoding(String link, String encoding) {
        String url = ""+link;
        StringBuffer content = new StringBuffer();
        URL resourceUrl, base, next;
        HttpURLConnection conn;
        String location;

        try {
            while (true) {
                resourceUrl = new URL(url);

                conn = (HttpURLConnection) resourceUrl.openConnection();

                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
                conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

                switch (conn.getResponseCode()) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        location = conn.getHeaderField("Location");
                        location = URLDecoder.decode(location, "UTF-8");
                        base = new URL(url);
                        next = new URL(base, location);  // Deal with relative URLs
                        url = next.toExternalForm();
                        continue;
                }

                break;

            }

            int status = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            conn.disconnect();

            return content.toString();
        } catch (Exception e) {
        e.printStackTrace();
        //throw new Exception("не могу получить совет для тебя");
    }
        return "";
    }


    public String decodeTitle_buffered(String title, String encoding) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Reader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(title.getBytes()), encoding));
        Writer w = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

        char[] buffer=new char[4096];
        int len;
        while((len=r.read(buffer)) != -1) {
            w.write(buffer, 0, len);
        }
        r.close();
        w.flush();
        w.close();

        String encodedTitle = out.toString();

        return encodedTitle;
    }

    String decodeTitle(String title, String encoding) throws UnsupportedEncodingException {
        return new String(title.getBytes("UTF-8"), encoding);
    }

    public String getCharsetFromHeaders(String contentType){
        if (contentType != null && contentType.toLowerCase().trim().contains("charset=")){
            String[] parts = contentType.toLowerCase().trim().split("=");
            if (parts.length > 0)
                return parts[1];
        }
        return null;
    }

    public static String getCharsetFromBody(String body) {
        if (body != null) {
            int headEnd = body.toLowerCase().trim().indexOf("</head>");

            // return null if there's no head tags
            if (headEnd == -1)
                return null;

            String body_head = body.toLowerCase().substring(0, headEnd);

            Pattern p = Pattern.compile("charset=([\"\'a-z0-9A-Z-]+)");
            Matcher m = p.matcher(body_head);
            String str_match = "";
            if (m.find()) {
                str_match = m.toMatchResult().group(1);
                return str_match.replaceAll("[\"']", "");
            }
        }
        return null;
    }
}
