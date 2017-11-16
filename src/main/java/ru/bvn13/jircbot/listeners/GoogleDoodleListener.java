package ru.bvn13.jircbot.listeners;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.model.GoogleDoodleSettings;

import static ru.bvn13.jircbot.config.JircBotConfiguration.KEY_GOOGLE_DOODLE;

public class GoogleDoodleListener extends ListenerAdapter {

    private static final String COMMAND = "?doodle";

    private JircBotConfiguration config;

    public GoogleDoodleListener(JircBotConfiguration config) {
        this.config = config;
    }


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getNick().equals(event.getBot().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        GoogleDoodleSettings settings = (GoogleDoodleSettings) this.config.getListenersSettings().get(KEY_GOOGLE_DOODLE);

        Document doc = null;
        try {
            doc = Jsoup.connect(settings.getCheckUrl()).get();
        } catch (Exception ex) {
            ex.printStackTrace();
            event.respond("ERROR retrieving url: "+settings.getCheckUrl());
            return;
        }

        Element elUrl = null;
        Element elTitle = null;
        Element elDate = null;
        try {
            elUrl = doc.select(settings.getLinkSelector()).first();
            elTitle = doc.select(settings.getTitleSelector()).first();
            elDate = doc.select(settings.getDateSelector()).first();
        } catch (Exception ex) {
            ex.printStackTrace();
            event.respond("ERROR: check selectors: ");
            return;
        }

        event.respond(String.format("Today: %s - %s (%s%s)",
                elDate.val(),
                elTitle.val(),
                settings.getMainUrl(),
                elUrl.attr("href")));

    }

}
