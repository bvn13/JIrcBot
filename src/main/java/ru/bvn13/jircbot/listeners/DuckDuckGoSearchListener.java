package ru.bvn13.jircbot.listeners;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;


@Component
public class DuckDuckGoSearchListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final String COMMAND = "?s";

    @Autowired
    private InternetAccessor internetAccessor;

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Autowired
    public DuckDuckGoSearchListener(DocumentationProvider documentationProvider) {
        this.registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("DuckDuckGoSearchListener")
                .setModuleDescription("Make a search in DuckDuckGo for you")
                .addCommand(CommandDescription.builder()
                        .command("s")
                        .description("Search it")
                        .example("?s [WHAT YOU WANT TO SEARCH]")
                        .build()
                );
    }


    @Override
    public void onMessage(MessageEvent event) throws Exception {
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

    private String search(String phrase) throws Exception {
        String link = "https://duckduckgo.com/lite/";
        Map<String, String> data = new HashMap<>();
        data.put("q", phrase);

        String queryPage = internetAccessor.sendPost(link, data);

        Document doc = Jsoup.parse(queryPage);

        Element linkElement = doc.select("a.result-link").first();
        if (linkElement != null) {
            String linkUrl = linkElement.attr("href");
            Element descrElement = doc.select(".result-snippet").first();
            String description = descrElement.text();
            return String.format("%s / %s", URLDecoder.decode(linkUrl, "utf-8"), description);
        } else {
            return "not found";
        }

    }

}
