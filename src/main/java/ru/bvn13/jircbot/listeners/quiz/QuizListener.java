package ru.bvn13.jircbot.listeners.quiz;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.fsm.Exceptions.FSMException;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn13 on 26.01.2018.
 */
@Component
public class QuizListener extends ImprovedListenerAdapter {

    private Map<String, QuizDialog> dialogs = new HashMap<>();

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getQuizEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        if (!event.getMessage().startsWith(QuizDialog.COMMAND.trim())) {
            return;
        }

        String channel = ((MessageEvent) event).getChannel().getName();
        QuizDialog dialog = null;
        if (!dialogs.containsKey(channel)) {
            try {
                dialog = QuizDialog.createDialog();
                dialogs.put(channel, dialog);
            } catch (FSMException e) {
                e.printStackTrace();
                return;
            }
        }
        dialog = dialogs.get(channel);
        dialog.setEvent(event);
        dialog.processCommand(event.getMessage());
    }

}
