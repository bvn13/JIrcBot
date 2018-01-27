package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import ru.bvn13.jircbot.listeners.advices.AdviceEngine;

/**
 * Created by bvn13 on 27.01.2018.
 */
public class AutoRejoinListener extends ListenerAdapter {

    private Boolean wasKicked = false;
    private String offender = "";

    @Override
    public void onKick(KickEvent event) throws Exception {

        if (event.getRecipient().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            wasKicked = true;
            offender = event.getUser().getNick();
            event.getBot().sendIRC().joinChannel(event.getChannel().getName());
        }

    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {

        if (wasKicked) {
            wasKicked = false;
            event.getBot().sendIRC().notice(event.getChannel().getName(), ""+offender+", "+ AdviceEngine.getAdvice());
        }

    }
}
