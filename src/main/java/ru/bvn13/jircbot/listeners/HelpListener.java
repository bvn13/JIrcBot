package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.config.JircBotConfiguration;
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
public class HelpListener extends ImprovedListenerAdapter {

    private static final List<String> COMMAND = Arrays.asList("?help", "?h");

    private JircBotConfiguration configuration;
    private DocumentationProvider documentationProvider;

    private String adviceToFolowMainUrl;

    @PostConstruct
    private void init() {
        adviceToFolowMainUrl = String.format("see all docs: %s/docs", configuration.getMainUrl());
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

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
            event.respond(adviceToFolowMainUrl);
        } else {
            answerWithHelp(event, message);
        }

    }

    private void answerWithHelp(MessageEvent event, String message) {
        String[] words = message.replace("  ", "").split(" ");
        if (words.length > 1) {
            event.respond(String.format("help syntax: ?help | ?help <COMMAND> | Commands: %s", documentationProvider.getAllCommands()));
        } else {
            Optional<ListenerDescription.CommandDescription> description = documentationProvider.findByCommand(words[0]);
            if (description.isPresent()) {
                ListenerDescription.CommandDescription d = description.get();
                event.respond(String.format("COMMAND: %s, DESCRIPTION: %s, EXAMPLE: %s", d.getCommand(), d.getDescription(), d.getExample()));
            } else {
                event.respond(String.format("wrong command %s. %s%s", words[0], adviceToFolowMainUrl.substring(0, 1).toUpperCase(), adviceToFolowMainUrl.substring(1)));
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
}
