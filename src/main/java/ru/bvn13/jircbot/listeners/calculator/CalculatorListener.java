package ru.bvn13.jircbot.listeners.calculator;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CalculatorListener extends ListenerAdapter {

    private Map<UUID, CalculatorDialog> dialogs = new HashMap<>();


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (!event.getMessage().startsWith(CalculatorDialog.COMMAND.trim())) {
            return;
        }

        CalculatorDialog dialog = null;
        if (this.dialogs.containsKey(event.getUser().getUserId())) {
            dialog = this.dialogs.get(event.getUser().getUserId());
        } else {
            dialog = CalculatorDialog.createDialog();
            this.dialogs.put(event.getUser().getUserId(), dialog);
        }
        dialog.setEvent(event);
        dialog.processCommand(event.getMessage());

    }






}
