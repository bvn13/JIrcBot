package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by bvn13 on 15.03.2019.
 */
@Component
public class HelpListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final List<String> COMMAND = Arrays.asList("?help", "?h");

    private JircBotConfiguration configuration;
    private DocumentationProvider documentationProvider;

    private String adviceToFollowMainUrl;

    @PostConstruct
    private void init() {
        adviceToFollowMainUrl = String.format("see all docs: %s/docs", configuration.getMainUrl());
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

        answer(event);
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {
        super.onPrivateMessage(event);

        answer(event);
    }

    private void answer(GenericMessageEvent event) {
        boolean isHelp = false;
        String command = "";
        for (String c : COMMAND) {
            if (event.getMessage().startsWith(c)) {
                isHelp = true;
                command = c;
                break;
            }
        }

        if (!isHelp) {
            return;
        }

        String message = event.getMessage().replace(command, "").trim();

        if (message.isEmpty()) {
            event.respond(adviceToFollowMainUrl);
        } else {
            answerWithHelp(event, message);
        }
    }

    private void answerWithHelp(GenericMessageEvent event, String message) {
        String[] words = message.replace("  ", "").split(" ");
        if (words.length > 1 || words[0].equalsIgnoreCase("all")) {
            event.respond(String.format("help syntax: ?help | ?help <COMMAND> | Commands: %s", documentationProvider.getAllCommands()));
        } else {
            Optional<ListenerDescription.CommandDescription> description = documentationProvider.findByCommand(words[0].toLowerCase());
            if (description.isPresent()) {
                ListenerDescription.CommandDescription d = description.get();
                event.respond(String.format("COMMAND: %s, DESCRIPTION: %s, EXAMPLE: %s", d.getCommand(), d.getDescription(), d.getExample()));
            } else {
                event.respond(String.format("wrong command %s. %s%s", words[0], adviceToFollowMainUrl.substring(0, 1).toUpperCase(), adviceToFollowMainUrl.substring(1)));
            }
        }
    }

    @Autowired
    public void setDocumentationProvider(DocumentationProvider documentationProvider) {
        this.documentationProvider = documentationProvider;
    }

    @Autowired
    public void setConfiguration(JircBotConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("HelpListener")
                .setModuleDescription("Provides a help")
                .addCommand(ListenerDescription.CommandDescription.builder()
                        .command("help")
                        .description("Provides help of command usage")
                        .example("?h[elp] all|<command>")
                        .build()
                );
    }

    @Override
    public void registerDescription(DocumentationProvider documentationProvider) {
        documentationProvider.register(this);
    }
}
