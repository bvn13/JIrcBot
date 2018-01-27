package ru.bvn13.jircbot.listeners.advices;

import com.google.common.collect.ImmutableSortedSet;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bvn13 on 23.01.2018.
 */
public class AdviceListener extends ListenerAdapter {

    private static final String COMMAND = "?advice";


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        if (event.getMessage().equalsIgnoreCase(COMMAND)) {
            try {
                String advice = AdviceEngine.getAdvice();
                event.respond(advice);
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
