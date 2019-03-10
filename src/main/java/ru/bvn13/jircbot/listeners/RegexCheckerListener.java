package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexCheckerListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?regex ";

    private static final Map<String, Boolean> usersState = new HashMap<>();

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onMessage(final MessageEvent event) throws Exception {
        super.onMessage(event);

        //TODO: rework with FSM

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getRegexCheckerEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        String message = event.getMessage().substring(COMMAND.length()).trim();

        if (message.isEmpty()) {
            event.respond("REGEX: expression is needed");
            return;
        }

        synchronized (usersState) {
            if (usersState.containsKey(event.getUser().getIdent()) && usersState.get(event.getUser().getIdent())) {
                return;
            }
            usersState.put(event.getUser().getIdent(), true);
        }

        String patternString = message;

        event.respond("EXPRESSION: "+message);
        WaitForQueue queue = new WaitForQueue(event.getBot());
        while (true) {

            MessageEvent currentEvent = queue.waitFor(MessageEvent.class);

            if (currentEvent.getMessage().startsWith(COMMAND)) {
                message = currentEvent.getMessage().substring(COMMAND.length()).trim();
                if (!message.isEmpty()) {
                    if (message.equalsIgnoreCase("done")) {
                        synchronized (usersState) {
                            usersState.put(event.getUser().getIdent(), false);
                        }
                        currentEvent.respond("REGEX: done");
                        queue.close();
                        return;
                    }
                    Pattern pattern = Pattern.compile(patternString);
                    if (pattern.matcher(message).matches()) {
                        Matcher matcher = pattern.matcher(message);
                        currentEvent.respond("FIND: "+pattern.matcher(message).find());
                        currentEvent.respond("GROUPS COUNT: "+pattern.matcher(message).groupCount());
                        int i = 0;
                        while (matcher.find()) {
                            currentEvent.respond(String.format("GROUP %d: %s\n", i++, matcher.group()));
                        }
                        if (i==0) {
                            currentEvent.respond("Matches, but groups were not found: " + message);
                        }
                    } else {
                        currentEvent.respond("Does not match: " + message);
                    }
                }
            }
        }

    }

}
