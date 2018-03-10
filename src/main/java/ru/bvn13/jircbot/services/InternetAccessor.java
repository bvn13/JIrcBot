package ru.bvn13.jircbot.services;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

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
        StringBuffer content = new StringBuffer();
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
        StringBuffer content = new StringBuffer();
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
        StringBuffer content = new StringBuffer();
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
        StringBuffer content = new StringBuffer();
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

}
