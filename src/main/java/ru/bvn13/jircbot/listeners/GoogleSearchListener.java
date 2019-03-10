package ru.bvn13.jircbot.listeners;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;
import ru.bvn13.jircbot.services.InternetAccessor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;


/**
 * Created by bvn13 on 06.02.2018.
 */
@Component
public class GoogleSearchListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final String COMMAND = "?gs";

    @Autowired
    private InternetAccessor internetAccessor;

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Autowired
    public GoogleSearchListener(DocumentationProvider documentationProvider) {
        this.registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("GoogleSearchListener")
                .setModuleDescription("Make a search in Google for you")
                .addCommand(CommandDescription.builder()
                        .command("gs")
                        .description("Search it")
                        .example("?gs [WHAT YOU WANT TO SEARCH]")
                        .build()
                );
    }


    @Override
    public void onMessage(final MessageEvent event) throws Exception {

        super.onMessage(event);

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), this.getChannelName(event)).getGoogleSearchEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        String message = event.getMessage().replace(COMMAND, "").trim();

        String result = search(message);

        event.respond(result);
    }


    private String search(String phrase) throws UnsupportedEncodingException {

        String encodedPhrase = URLEncoder.encode(phrase.replaceAll(" ", "+"), "utf-8");
        String link = "https://google.ru/search?q="+encodedPhrase;
        String queryPage = internetAccessor.retrieveContentByLink(link);

        Document doc = Jsoup.parse(queryPage);

        Elements searchResults = doc.select("#res #search #ires .g");
        Element firstResult = searchResults.first();
        Element descrElement = firstResult.select(".s .st").first();
        String description = descrElement.text();
        Element linkToRedirectPage = firstResult.select(".r a").first();
        String linkTitle = linkToRedirectPage.text();
        String redirectPage = internetAccessor.retrieveContentByLink("https://google.ru"+linkToRedirectPage.attr("href"));
        String destinationUrl = null;
        try {
            Document redirectDoc = Jsoup.parse(redirectPage);
            destinationUrl = redirectDoc.select("._jFe a").first().attr("href");
        } catch (Exception e) {
            try {
                destinationUrl = internetAccessor.getLastUrl("https://google.ru"+linkToRedirectPage.attr("href"));
                if (destinationUrl == null || destinationUrl.isEmpty()) {
                    destinationUrl = "https://google.ru"+linkToRedirectPage.attr("href");
                }
            } catch (Exception e1) {
                destinationUrl = "https://google.ru"+linkToRedirectPage.attr("href");
            }
        }



        return String.format("%s / %s / %s", URLDecoder.decode(destinationUrl, "utf-8"), linkTitle, description);
    }

}
