package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.Event;
import org.pircbotx.hooks.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
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
        return channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getLoggingEnabled();
    }

    public boolean isEnabled(String serverName, String channelName) throws Exception {
        return channelSettingsService.getChannelSettings(serverName, channelName).getLoggingEnabled();
    }

    @Override
    public void onEvent(Event event) throws Exception {
        super.onEvent(event);
    }


    @Override
    public void onJoin(JoinEvent event) throws Exception {
        super.onJoin(event);

        if (!isEnabled(event)) return;

        synchronized (onlineUsers) {
            if (!onlineUsers.containsKey(event.getChannel().getName())) {
                onlineUsers.put(event.getChannel().getName(), new HashSet<>());
            }
            Set<String> users = onlineUsers.get(event.getChannel().getName());
            event.getChannel().getUsers().forEach(user -> {
                if (!users.contains(user.getNick().toLowerCase())) {
                    users.add(user.getNick().toLowerCase());
                }
            });
        }

        log(event.getBot().getServerHostname(), event.getChannel().getName(), "User joined: "+event.getUser().getNick());
    }

    @Override
    public void onPart(PartEvent event) throws Exception {
        super.onPart(event);

        if (!isEnabled(event)) return;
        log(event.getBot().getServerHostname(), event.getChannel().getName(), "User " + event.getUser().getNick() + " quit (" + event.getReason() + ")");
        synchronized (onlineUsers) {
            for (String channelName : onlineUsers.keySet()) {
                Set<String> users = onlineUsers.get(channelName);
                if (users.contains(event.getUser().getNick().toLowerCase())) {
                    users.remove(event.getUser().getNick().toLowerCase());
                }
            }
        }
    }

    @Override
    public void onQuit(QuitEvent event) throws Exception {
        super.onQuit(event);

        List<String> channels = new ArrayList<>();
        synchronized (onlineUsers) {
            for (String channelName : onlineUsers.keySet()) {
                Set<String> users = onlineUsers.get(channelName);
                if (users.contains(event.getUser().getNick().toLowerCase())) {
                    if (isEnabled(JircBot.extractServer(event.getBot().getServerHostname()), channelName)) {
                        log(event.getBot().getServerHostname(), channelName, "User " + event.getUser().getNick() + " quit (" + event.getReason() + ")");
                        users.remove(event.getUser().getNick().toLowerCase());
                    }
                }
            }
        }
    }

    @Override
    public void onKick(KickEvent event) throws Exception {
        super.onKick(event);

        if (!isEnabled(event)) return;

        if (onlineUsers.containsKey(event.getChannel().getName())) {
            onlineUsers.get(event.getChannel().getName()).remove(event.getUser().getNick().toLowerCase());
        }
        log(event.getBot().getServerHostname(), event.getChannel().getName(), "User "+event.getRecipient().getNick()+" was kicked by "+event.getUser().getNick()+" by reason: "+event.getReason());
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);

        if (!isEnabled(event)) return;
        log(event.getBot().getServerHostname(), event.getChannel().getName(), event.getUser().getNick(), event.getMessage());
    }

    @Override
    public void onNickChange(NickChangeEvent event) throws Exception {
        super.onNickChange(event);

        List<String> channels = new ArrayList<>();
        for (String channelName : onlineUsers.keySet()) {
            Set<String> users = onlineUsers.get(channelName);
            if (users.contains(event.getUser().getNick().toLowerCase())) {
                if (isEnabled(JircBot.extractServer(event.getBot().getServerHostname()), channelName)) {
                    log(event.getBot().getServerHostname(), channelName,"User "+event.getOldNick()+" is now known as "+event.getNewNick());
                    users.remove(event.getUser().getNick().toLowerCase());
                }
            }
        }
    }

    @Override
    public void onNotice(NoticeEvent event) throws Exception {
        super.onNotice(event);

        if (!isEnabled(event)) return;
        log(event.getBot().getServerHostname(), event.getChannel().getName(), event.getMessage());
    }

    @Override
    public void onTopic(TopicEvent event) throws Exception {
        super.onTopic(event);

        if (!isEnabled(event)) return;
        log(event.getBot().getServerHostname(), event.getChannel().getName(), ""+event.getUser().getNick()+" set topic: "+event.getTopic());
    }

    @Override
    public void onAction(ActionEvent event) throws Exception {
        super.onAction(event);

        if (!isEnabled(event)) return;
        log(event.getBot().getServerHostname(), event.getChannel().getName(), "*"+event.getUser().getNick()+" "+event.getAction());
    }

    @Override
    public void onOutput(OutputEvent event) throws Exception {
        super.onOutput(event);

        if (!isEnabled(JircBot.extractServer(event.getBot().getServerHostname()), event.getLineParsed().get(1))) return;
        switch (event.getLineParsed().get(0)) {
            case "PRIVMSG" :
            case "NOTICE" :
                log(event.getBot().getServerHostname(), event.getLineParsed().get(1), event.getLineParsed().get(2));
        }
    }

    private void log(String serverHost, String channelName, String username, String message) {
        IrcMessage msg = new IrcMessage(serverHost, channelName, username, message);
        ircMessageService.save(msg);
    }

    private void log(String serverHost, String channelName, String message) {
        IrcMessage msg = new IrcMessage(serverHost, channelName, message);
        ircMessageService.save(msg);
    }

}
