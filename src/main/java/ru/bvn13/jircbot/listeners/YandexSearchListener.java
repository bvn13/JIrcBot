package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import ru.bvn13.jircbot.Services.YandexSearchService;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.model.YandexSearchSettings;

import static ru.bvn13.jircbot.config.JircBotConfiguration.KEY_YANDEX_SEARCH;

public class YandexSearchListener extends ListenerAdapter {

    private static final String COMMAND = "?search ";


    private YandexSearchSettings config;

    private YandexSearchService yandexSearchService;

    public YandexSearchListener(JircBotConfiguration config, YandexSearchService yandexSearchService) {
        this.config = (YandexSearchSettings) config.getListenersSettings().get(KEY_YANDEX_SEARCH);
        this.yandexSearchService = yandexSearchService;
        this.yandexSearchService.setKey(this.config.getKey());
        this.yandexSearchService.setUser(this.config.getUser());
        this.yandexSearchService.setUrl(this.config.getUrl());
    }


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        String message = event.getMessage().substring(COMMAND.length()).trim();

        try {
            final YandexSearchService.YaPage result = this.yandexSearchService.loadYaPage(message, 0);
            int i = 0;
            for (YandexSearchService.YaItem item : result.getYaItems()) {
                if (i++ == 0) {
                    event.respond(String.format("%s - %s (%s)", item.getUrl(), item.getTitle(), item.getDescription()));
                    //event.respond("Next entries were sended privately.");
                } else {
                    event.respondPrivateMessage(String.format("%d. %s - %s (%s)", (i - 1), item.getUrl(), item.getTitle(), item.getDescription()));
                }
            }
            if (i == 0) {
                event.respond("Not found");
            }
        } catch (Exception exp) {
            exp.printStackTrace();
            event.respond("ERROR has been occurred. Try again later.");
        }
    }

}
