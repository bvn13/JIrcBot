package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.stereotype.Component;

@Component
public class PingPongListener extends ListenerAdapter {

    private static final String COMMAND = "?ping";

    @Override
    public void onMessage(final MessageEvent event) throws Exception {
        super.onMessage(event);

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        event.respond("pong!");

    }

}
