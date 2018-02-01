package ru.bvn13.jircbot.bot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericEvent;

/**
 * Created by bvn13 on 31.01.2018.
 */
public class ImprovedListenerAdapter extends ListenerAdapter {

    protected void sendNotice(GenericEvent event, String str) {
        event.getBot().sendIRC().notice(((MessageEvent) event).getChannel().getName(), str);
    }

    protected String getChannelName(GenericEvent event) {
        return ((MessageEvent) event).getChannel().getName();
    }

}
