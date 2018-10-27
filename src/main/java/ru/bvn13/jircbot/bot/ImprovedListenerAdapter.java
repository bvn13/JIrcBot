package ru.bvn13.jircbot.bot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelEvent;
import org.pircbotx.hooks.types.GenericEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by bvn13 on 31.01.2018.
 */
public class ImprovedListenerAdapter extends ListenerAdapter {

    protected void sendNotice(GenericEvent event, String str) {
        event.getBot().sendIRC().notice(((MessageEvent) event).getChannel().getName(), str);
    }

    protected String getChannelName(GenericEvent event) {
        if (event instanceof GenericChannelEvent) {
            return ((GenericChannelEvent) event).getChannel().getName();
        } else {
            return ((MessageEvent) event).getChannel().getName();
        }
    }

    protected boolean isUserOnline(GenericEvent event, String username) {

        List<String> usersNicks = event.getBot().getUserChannelDao().getAllUsers().stream()
                .map(u -> u.getNick().toLowerCase()).collect(Collectors.toList());

        return usersNicks.contains(username.toLowerCase());

    }

}
