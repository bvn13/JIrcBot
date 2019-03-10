package ru.bvn13.jircbot.listeners.calculator;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CalculatorListener extends ImprovedListenerAdapter {

    private Map<UUID, CalculatorDialog> dialogs = new HashMap<>();

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event)).getCalculatorEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
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
