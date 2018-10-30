package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;

@Component
public class PingPongListener extends ListenerAdapter implements DescriptionProvided {

    private static final String COMMAND = "?ping";

    @Override
    public void onMessage(final MessageEvent event) throws Exception {

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        event.respond("pong!");

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
