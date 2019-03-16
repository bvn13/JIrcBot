package ru.bvn13.jircbot.bot;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericChannelEvent;
import org.pircbotx.hooks.types.GenericEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.util.List;
import java.util.function.Consumer;
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

        String channel = getChannelName(event);

        List<String> usersNicks = event.getBot().getUserChannelDao().getChannel(channel).getUsers().stream()
                .map(u -> u.getNick().toLowerCase()).collect(Collectors.toList());

        return usersNicks.contains(username.toLowerCase());

    }

    protected boolean isApplicable(GenericMessageEvent event, String command) {
        return isApplicable(event, command);
    }

    protected boolean isApplicable(GenericMessageEvent event, String command, Consumer<String> callback) {
        String[] words = event.getMessage().split(" ");
        if (words.length > 0) {
            if (words[0].equalsIgnoreCase(command)) {
                if (callback != null) {
                    callback.accept(event.getMessage().substring(words[0].length()).trim());
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isApplicable(GenericMessageEvent event, List<String> commands) {
        return isApplicable(event, commands, null);
    }

    protected boolean isApplicable(GenericMessageEvent event, List<String> commands, Consumer<String> callback) {
        String[] words = event.getMessage().split(" ");
        if (words.length > 0) {
            boolean isApplicable = false;
            String command = "";
            for (String c : commands) {
                if (words[0].equalsIgnoreCase(c)) {
                    isApplicable = true;
                    command = words[0];
                    break;
                }
            }

            if (isApplicable) {
                if (callback != null) {
                    callback.accept(event.getMessage().substring(command.length()).trim());
                }
                return true;
            }
        }
        return false;
    }
}
