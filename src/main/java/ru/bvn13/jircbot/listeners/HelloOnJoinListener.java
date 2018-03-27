package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.entities.ChannelSettings;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;

/**
 * Created by bvn13 on 25.01.2018.
 */
@Component
public class HelloOnJoinListener extends ImprovedListenerAdapter {

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onJoin(final JoinEvent event) throws Exception {

        ChannelSettings channelSettings = channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getUserBot().getServer()), getChannelName(event));

        if (!channelSettings.getHelloOnJoinEnabled()) {
            return;
        }

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (channelSettings.getOnJoinMessage() != null && !channelSettings.getOnJoinMessage().isEmpty()) {
            event.respond(channelSettings.getOnJoinMessage().replace("%nick%", event.getUser().getNick()));
        } else {
            event.respond("Привет, " + event.getUser().getNick() + "!");
        }
    }


}
