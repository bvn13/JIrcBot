package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.listeners.advices.AdviceEngine;

/**
 * Created by bvn13 on 27.01.2018.
 */
@Component
public class AutoRejoinListener extends ImprovedListenerAdapter {

    @Autowired
    private ChannelSettingsService channelSettingsService;

    private Boolean wasKicked = false;
    private String offender = "";

    @Override
    public void onKick(KickEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getUserBot().getServer()), event.getChannel().getName()).getAutoRejoinEnabled()) {
            return;
        }

        if (event.getRecipient().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            wasKicked = true;
            offender = event.getUser().getNick();
            event.getBot().sendIRC().joinChannel(event.getChannel().getName());
        }

    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getUserBot().getServer()), event.getChannel().getName()).getAutoRejoinEnabled()) {
            return;
        }

        if (wasKicked) {
            wasKicked = false;
            this.sendNotice(event, ""+offender+", "+ AdviceEngine.getAdvice());
        }

    }
}
