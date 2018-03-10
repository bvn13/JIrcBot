package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.database.entities.IrcMessage;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.database.services.IrcMessageService;

import java.util.*;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Component
public class LoggerListener extends ImprovedListenerAdapter {

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Autowired
    private IrcMessageService ircMessageService;

    private Map<String, Set<String>> onlineUsers = new HashMap<>();




    public boolean isEnabled(Event event) throws Exception {
        return channelSettingsService.getChannelSettings(getChannelName(event)).getLoggingEnabled();
    }

    public boolean isEnabled(String channelName) throws Exception {
        return channelSettingsService.getChannelSettings(channelName).getLoggingEnabled();
    }


    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (!isEnabled(event)) return;

        if (!onlineUsers.containsKey(event.getChannel().getName())) {
            onlineUsers.put(event.getChannel().getName(), new HashSet<>());
        }
        Set<String> users = onlineUsers.get(event.getChannel().getName());
        event.getChannel().getUsers().forEach(user -> {
            users.add(user.getNick().toLowerCase());
        });
        log(event.getChannel().getName(), "User joined: "+event.getUser().getNick());
    }

    @Override
    public void onQuit(QuitEvent event) throws Exception {
        List<String> channels = new ArrayList<>();
        for (String channelName : onlineUsers.keySet()) {
            Set<String> users = onlineUsers.get(channelName);
            if (users.contains(event.getUser().getNick().toLowerCase())) {
                if (isEnabled(channelName)) {
                    log(channelName, "User " + event.getUser().getNick() + " quit (" + event.getReason() + ")");
                    users.remove(event.getUser().getNick().toLowerCase());
                }
            }
        }
    }

    @Override
    public void onKick(KickEvent event) throws Exception {
        if (!isEnabled(event)) return;

        if (onlineUsers.containsKey(event.getChannel().getName())) {
            onlineUsers.get(event.getChannel().getName()).remove(event.getUser().getNick().toLowerCase());
        }
        log(event.getChannel().getName(), "User "+event.getRecipient().getNick()+" was kicked by "+event.getUser().getNick()+" by reason: "+event.getReason());
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        if (!isEnabled(event)) return;

        log(event.getChannel().getName(), event.getUser().getNick(), event.getMessage());
    }

    @Override
    public void onNickChange(NickChangeEvent event) throws Exception {
        List<String> channels = new ArrayList<>();
        for (String channelName : onlineUsers.keySet()) {
            Set<String> users = onlineUsers.get(channelName);
            if (users.contains(event.getUser().getNick().toLowerCase())) {
                if (isEnabled(channelName)) {
                    log(channelName,"User "+event.getOldNick()+" is now known as "+event.getNewNick());
                    users.remove(event.getUser().getNick().toLowerCase());
                }
            }
        }
    }

    @Override
    public void onNotice(NoticeEvent event) throws Exception {
        if (!isEnabled(event)) return;
        log(event.getChannel().getName(), event.getMessage());
    }

    @Override
    public void onTopic(TopicEvent event) throws Exception {
        if (!isEnabled(event)) return;
        log(event.getChannel().getName(), ""+event.getUser().getNick()+" set topic: "+event.getTopic());
    }

    private void log(String channelName, String username, String message) {
        IrcMessage msg = new IrcMessage(channelName, username, message);
        ircMessageService.save(msg);
    }

    private void log(String channelName, String message) {
        IrcMessage msg = new IrcMessage(channelName, message);
        ircMessageService.save(msg);
    }

    private void log(String message) {
        IrcMessage msg = new IrcMessage(message);
        ircMessageService.save(msg);
    }

}
