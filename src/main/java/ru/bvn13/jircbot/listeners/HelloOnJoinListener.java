package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
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

        if (!channelSettingsService.getChannelSettings(getChannelName(event)).getHelloOnJoinEnabled()) {
            return;
        }

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        //event.respond("Привет, "+event.getUser().getNick()+"!");
        this.sendNotice(event, "Привет, "+event.getUser().getNick()+"!");
    }


}
