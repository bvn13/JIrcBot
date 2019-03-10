package ru.bvn13.jircbot.services;

import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by bvn13 on 06.02.2018.
 */
@Component
public class InternetAccessor {

    public String retrieveContentByLink(String link) {
        return retrieveContentByLinkWithEncoding(link, "UTF-8");
    }

    public String retrieveContentByLinkWithEncoding(String link, String encoding) {
        if (link.startsWith("https")) {
            return retrieveContentByLinkWithEncoding_https(link, encoding);
        } else {
            return retrieveContentByLinkWithEncoding_http(link, encoding);
        }
    }

    public String getLastUrl(String link) {
        if (link.startsWith("https")) {
            return getLastUrl_https(link);
        } else {
            return getLastUrl_http(link);
        }
    }

    private String getLastUrl_http(String link) {
        String url = ""+link;
        StringBuilder content = new StringBuilder();
        URL resourceUrl, base, next;
        HttpURLConnection conn;
        String location = link;

        try {
            while (true) {
                if (location != null && location.startsWith("https://")) {
                    return getLastUrl_https(location);
                }

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

            return location;
        } catch (Exception e) {
            e.printStackTrace();
            //throw new Exception("не могу получить совет для тебя");
        }
        return "";

    }

    private String getLastUrl_https(String link) {
        String url = ""+link;
        StringBuilder content = new StringBuilder();
        URL resourceUrl, base, next;
        HttpsURLConnection conn;
        String location = link;

        try {
            while (true) {

                if (location != null && location.startsWith("http://")) {
                    return getLastUrl_http(location);
                }

                resourceUrl = new URL(url);

                conn = (HttpsURLConnection) resourceUrl.openConnection();

                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
                conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

                switch (conn.getResponseCode()) {
                    case HttpsURLConnection.HTTP_MOVED_PERM:
                    case HttpsURLConnection.HTTP_MOVED_TEMP:
                        location = conn.getHeaderField("Location");
                        location = URLDecoder.decode(location, "UTF-8");
                        base = new URL(url);
                        next = new URL(base, location);  // Deal with relative URLs
                        url = next.toExternalForm();
                        continue;
                }

                break;

            }

            return location;
        } catch (Exception e) {
            e.printStackTrace();
            //throw new Exception("не могу получить совет для тебя");
        }
        return "";
    }

    private String retrieveContentByLinkWithEncoding_http(String link, String encoding) {
        String url = ""+link;
        StringBuilder content = new StringBuilder();
        URL resourceUrl, base, next;
        HttpURLConnection conn;
        String location = null;

        try {
            while (true) {
                if (location != null && location.startsWith("https://")) {
                    return retrieveContentByLinkWithEncoding_https(location, encoding);
                }

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

    private String retrieveContentByLinkWithEncoding_https(String link, String encoding) {
        String url = ""+link;
        StringBuilder content = new StringBuilder();
        URL resourceUrl, base, next;
        HttpsURLConnection conn;
        String location = null;

        try {
            while (true) {
                if (location != null && location.startsWith("http://")) {
                    return retrieveContentByLinkWithEncoding_http(location, encoding);
                }

                resourceUrl = new URL(url);

                conn = (HttpsURLConnection) resourceUrl.openConnection();

                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
                conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

                switch (conn.getResponseCode()) {
                    case HttpsURLConnection.HTTP_MOVED_PERM:
                    case HttpsURLConnection.HTTP_MOVED_TEMP:
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

    public String sendPost(String link, Map<String, String> data) throws Exception {
        URL url = new URL(link);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);

        data.put("kl", "us-en");

        http.setRequestProperty("user-agent", "Mozilla/5.0 (X11; Fedora; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        http.setRequestProperty("origin", "https://duckduckgo.com");
        http.setRequestProperty("referer", "https://duckduckgo.com/");

        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String,String> entry : data.entrySet()) {
            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }

        int responseCode = http.getResponseCode();
        if (responseCode >= 200 && responseCode < 400) {

            StringBuilder answer = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                answer.append(inputLine);
            }
            in.close();

            http.disconnect();
            return answer.toString();
        } else {
            return http.getResponseMessage();
        }
    }

}
