package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.database.entities.DeferredMessage;
import ru.bvn13.jircbot.database.services.DeferredMessageService;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Component
public class DeferredMessagesListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?tell";

    private static SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DeferredMessageService deferredMessageService;


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        //this.sendDeferredMessage(event);

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        String message = event.getMessage().replace(COMMAND, "").trim();
        String commands[] = message.split(" ", 2);

        if (commands.length != 2) {
            event.respond("Deferred messages usage: ?tell <UserNick/ME> your message");
            return;
        }

        String userName = event.getUser().getNick();
        if (commands[0].equalsIgnoreCase("me")) {
            // deferred to myself
            deferredMessageService.saveDeferredMessage(userName, userName.toLowerCase(), commands[1]);
            event.respond("Saved message to "+userName);
        } else {
            // deferred to somebody
            deferredMessageService.saveDeferredMessage(userName, commands[0].toLowerCase(), commands[1]);
            event.respond("Saved message to "+commands[0]);
        }

    }


    private void sendDeferredMessage(final GenericMessageEvent event) {

        List<DeferredMessage> deferredMessages = deferredMessageService.getDeferredMessagesForUser(event.getUser().getNick().toLowerCase());
        if (deferredMessages != null) {
            deferredMessages.forEach(msg -> {
                event.respond("User "+msg.getSender()+" at "+dt.format(msg.getCreatedAt())+" tell you: "+msg.getMessage());
                deferredMessageService.markMessageWasSent(msg);
            });
        }

    }

}