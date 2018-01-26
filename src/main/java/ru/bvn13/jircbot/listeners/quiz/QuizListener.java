package ru.bvn13.jircbot.listeners.quiz;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import ru.bvn13.fsm.Exceptions.FSMException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn13 on 26.01.2018.
 */
public class QuizListener extends ListenerAdapter {

    private Map<String, QuizDialog> dialogs = new HashMap<>();



    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {
        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
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
