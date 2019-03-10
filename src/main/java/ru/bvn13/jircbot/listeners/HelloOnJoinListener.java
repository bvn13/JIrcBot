package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.JoinEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.entities.ChannelSettings;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;


/**
 * Created by bvn13 on 25.01.2018.
 */
@Component
public class HelloOnJoinListener extends ImprovedListenerAdapter implements DescriptionProvided {

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onJoin(final JoinEvent event) throws Exception {

        super.onJoin(event);

        ChannelSettings channelSettings = channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), getChannelName(event));

        if (!channelSettings.getHelloOnJoinEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        if (channelSettings.getOnJoinMessage() != null && !channelSettings.getOnJoinMessage().isEmpty()) {
            event.respond(channelSettings.getOnJoinMessage().replace("%nick%", event.getUser().getNick()));
        } else {
            event.respond("Welcome, " + event.getUser().getNick() + "!");
        }
    }

    @Autowired
    public HelloOnJoinListener(DocumentationProvider documentationProvider) {
        registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("HelloOnJoinListener")
                .setModuleDescription("The bot greets everyone joining the channel\n"+
                        "You can set the greeting text using <a href='/docs#AdminListener'>Admin module</a>")
                ;
    }

}
