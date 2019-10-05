package ru.bvn13.jircbot.listeners;

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
import ru.bvn13.jircbot.utilities.WebTitleExtractor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;

/**
 * Created by bvn13 on 23.01.2018.
 */
@Component
public class LinkPreviewListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final Pattern REGEX = Pattern.compile("(?i)(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?");

    private ChannelSettingsService channelSettingsService;

    @Autowired
    public LinkPreviewListener(DocumentationProvider documentationProvider) {
        registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("LinkPreviewListener")
                .setModuleDescription("With this module enabled into <a href='/docs#AdminListener'>Admin module</a> the bot will send the title of every URL found in messages posted in channel")
                ;
    }


    @Override
    public void onMessage(final MessageEvent event) throws Exception {

        super.onMessage(event);

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getLinkPreviewEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        List<String> links = findLink(event.getMessage());
        for (String link : links) {
            //String info = parseLink(link);
            String title = WebTitleExtractor.getPageTitle(link);
            if (title != null && !title.isEmpty()) {
                //event.respond(info);
                event.getChannel().send().message("TITLE: "+title);
            }
        };

    }


    private List<String> findLink(String message) {
        Matcher matcher = REGEX.matcher(message);
        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group());
        }
        return links;
    }

    @Autowired
    public void setChannelSettingsService(ChannelSettingsService channelSettingsService) {
        this.channelSettingsService = channelSettingsService;
    }
}
