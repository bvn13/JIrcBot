package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;

/**
 * Created by bvn13 on 25.01.2018.
 */
public class HelloOnJoinListener extends ListenerAdapter {

    @Override
    public void onJoin(final JoinEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        //event.respond("hello!");
        event.getBot().sendIRC().notice(event.getChannel().getName(), "Привет, "+event.getUser().getNick()+"!");
    }


}
