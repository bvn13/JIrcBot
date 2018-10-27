package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.entities.DeferredMessage;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.database.services.DeferredMessageService;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Component
public class DeferredMessagesListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?tell";
    private static final String COMMAND_FORGET = "?forget";
    private static final String COMMAND_READ = "?read";

    private static SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Autowired
    private DeferredMessageService deferredMessageService;


    @Override
    public void onMessage(final MessageEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getDeferredMessagesEnabled()) {
            return;
        }

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        String userName = event.getUser().getNick();
        String channelName = this.getChannelName(event);
        String userIdent = event.getUser().getNick() + "!" + event.getUser().getLogin() + "@" + event.getUser().getHostname();

        if (event.getMessage().startsWith(COMMAND)) {
            String message = event.getMessage().replace(COMMAND, "").trim();
            String commands[] = message.split(" ", 2);

            if (commands.length != 2) {
                event.respond("Deferred messages usage: ?tell <UserNick/ME/Ident> your message here");
                return;
            }

            if (commands[0].equalsIgnoreCase("me") || userName.equalsIgnoreCase(commands[0])) {
                // deferred to myself
                deferredMessageService.saveDeferredMessage(channelName, userName, userName.toLowerCase(), commands[1]);
                event.respond("Saved message to "+userName);
            } else {
                if (commands[0].equalsIgnoreCase(event.getBot().getUserBot().getNick())) {
                    event.respond("Sorry, message cannot be deferred to me.");
                } else {
                    // deferred to somebody
                    if (isUserOnline(event, userName)) {
                        event.respond(String.format("%s is online, tell him/her directly, please.", commands[0]));
                    } else {
                        deferredMessageService.saveDeferredMessage(channelName, userName, commands[0].toLowerCase(), commands[1]);
                        event.respond("Saved message to " + commands[0]);
                    }
                }
            }
        } else if (event.getMessage().startsWith(COMMAND_FORGET)) {
            int count = deferredMessageService.forgetAllMessages(channelName, userName, userIdent);
            event.respond("All "+count+" messages to "+userName+" were deleted");
        } else if (event.getMessage().startsWith(COMMAND_READ)) {
            List<DeferredMessage> deferredMessages = deferredMessageService.getDeferredMessagesForUser(channelName, userName, userIdent);
            deferredMessages.forEach(msg -> {
                event.respondPrivateMessage("User "+msg.getSender()+" at "+dt.format(msg.getDtCreated())+" told you: "+msg.getMessage());
                deferredMessageService.markMessageWasSent(msg);
            });
        } else {
            this.sendDeferredMessage(event);
        }

    }


    private void sendDeferredMessage(final MessageEvent event) {

        String userIdent = event.getUser().getNick() + "!" + event.getUser().getLogin() + "@" + event.getUser().getHostname();

        List<DeferredMessage> deferredMessages = deferredMessageService.getDeferredMessagesForUser(this.getChannelName(event), event.getUser().getNick().toLowerCase(), userIdent);
        if (deferredMessages != null && deferredMessages.size() > 0) {
            DeferredMessage msg = deferredMessages.get(0);
            String more = "" + (deferredMessages.size() > 1 ? " ("+(deferredMessages.size()-1)+" message/-s more)" : "");
            event.respond("User "+msg.getSender()+" at "+dt.format(msg.getDtCreated())+" told you"+more+": "+msg.getMessage());
            deferredMessageService.markMessageWasSent(msg);
        }

    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        String userIdent = event.getUser().getNick() + "!" + event.getUser().getLogin() + "@" + event.getUser().getHostname();

        List<DeferredMessage> deferredMessages = deferredMessageService.getDeferredMessagesForUser(this.getChannelName(event), event.getUser().getNick().toLowerCase(), userIdent);
        if (deferredMessages != null && deferredMessages.size() > 0) {
            event.respond("You have "+deferredMessages.size()+" unread message(-s)");
        }

    }

}
