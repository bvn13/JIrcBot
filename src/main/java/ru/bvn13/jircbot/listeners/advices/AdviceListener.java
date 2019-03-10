package ru.bvn13.jircbot.listeners.advices;

import com.google.common.collect.ImmutableSortedSet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bvn13 on 23.01.2018.
 */
@Component
public class AdviceListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?advice";

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getAdvicesEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        if (event.getMessage().equalsIgnoreCase(COMMAND)) {
            try {
                String advice = AdviceEngine.getAdvice();
                if (advice.trim().isEmpty()) {
                    event.respond("советы кончились");
                } else {
                    event.respond(advice);
                }
            } catch (Exception e) {
                event.respond(e.getMessage());
            }
        } else {
            String userName = event.getMessage().replace(COMMAND, "").trim();
            if (userName.equalsIgnoreCase(event.getBot().getNick())) {
                event.respondPrivateMessage("я тебе посоветую щас, блеать!");
            } else {
                ImmutableSortedSet<User> users = null;
                if (event instanceof MessageEvent) {
                    users = ((MessageEvent) event).getChannel().getUsers();
                } else if (event instanceof NoticeEvent) {
                    users = ((NoticeEvent) event).getChannel().getUsers();
                } else {
                    event.respondPrivateMessage("я не понимаю сообщения такого типа");
                }
                if (this.userIsInList(users, userName)) {
                    try {
                        String advice = AdviceEngine.getAdvice();
                        String response = "" + userName + ", " + advice;
                        if (event instanceof MessageEvent) {
                            ((MessageEvent) event).respondChannel(response);
                        } else if (event instanceof NoticeEvent) {
                            ((NoticeEvent) event).respondChannel(response);
                        }
                    } catch (Exception e) {
                        event.respond(e.getMessage());
                    }
                } else {
                    event.respondPrivateMessage("нет пользователя " + userName);
                }
            }
        }

    }

    private boolean userIsInList(ImmutableSortedSet<User> users, String userName) {
        AtomicBoolean isOnline = new AtomicBoolean(false);
        users.forEach(u -> {
            User user = u;
            if (user.getNick().equalsIgnoreCase(userName)) {
                isOnline.set(true);
            }
        });
        return isOnline.get();
    }



}
