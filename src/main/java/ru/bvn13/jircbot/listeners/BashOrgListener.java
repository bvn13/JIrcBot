package ru.bvn13.jircbot.listeners;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pircbotx.hooks.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;

/**
 * Created by bvn13 on 26.01.2018.
 */
@Component
public class BashOrgListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final Logger logger = LoggerFactory.getLogger(BashOrgListener.class);

    private static final String COMMAND = "?bash";
    private static final String USER_AGENT = "Mozilla/5.0";

    private static final Pattern PATTERN_CHARSET = Pattern.compile(".*charset=(.*)$");

    private ChannelSettingsService channelSettingsService;

    @Autowired
    public BashOrgListener(DocumentationProvider documentationProvider) {
        this.registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("BashOrgListener")
                .setModuleDescription("Send bash.org quotes in channel on your request")
                .addCommand(CommandDescription.builder()
                        .command("bash")
                        .description("Send random quote from bash.org to channel")
                        .example("?bash")
                        .build()
                );
    }

    @Override
    public void onMessage(final MessageEvent event) throws Exception {
        super.onMessage(event);

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getBashOrgEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        isApplicable(event, COMMAND, message -> {
            try {
                event.respond(getRandomBashQuote());
            } catch (Exception e) {
                event.respond("ERROR: "+e.getMessage());
                logger.error("", e);
            }
        });

    }

    private String getDataFromConnection(HttpURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    private String getRandomBashQuote() throws Exception {
        URL obj = new URL("https://bash.im/random");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("Accept-Charset", "utf-8");
        con.setRequestProperty("User-Agent", USER_AGENT);
        //con.setRequestProperty("X-Requested-With", "XMLHttpRequest");

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Could not get a random quote!");
        }

//        String contentType = con.getContentType();
//        Matcher matcher = PATTERN_CHARSET.matcher(contentType);
//        String charset = "utf-8";
//        if (matcher.find()) {
//            charset = matcher.group(1);
//        }
        String response = getDataFromConnection(con);

        Document doc = Jsoup.parse(response);
        Elements quotes = doc.select("article.quote .quote__body");

        if (quotes.size() == 0) {
            throw new Exception("Nothing was received from bash.org!");
        }

        Element quote = quotes.get(0);

        return quote.text().replace("\n", "");

    }


    @Autowired
    public void setChannelSettingsService(ChannelSettingsService channelSettingsService) {
        this.channelSettingsService = channelSettingsService;
    }
}
