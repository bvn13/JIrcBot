package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;

@Component
public class PingPongListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final String COMMAND = "?ping";

    @Override
    public void onMessage(final MessageEvent event) throws Exception {
        super.onMessage(event);

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        isApplicable(event, COMMAND, message -> event.respond("pong!"));

    }

    @Autowired
    public PingPongListener(DocumentationProvider documentationProvider) {
        registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("PingPongListener")
                .setModuleDescription("Try to play ping-pong if you not aware of is the bot here")
                .addCommand(CommandDescription.builder()
                        .command("ping")
                        .description("Returns pong")
                        .example("?ping")
                        .build()
                );
    }

}
