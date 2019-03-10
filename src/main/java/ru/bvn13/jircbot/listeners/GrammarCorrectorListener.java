package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.entities.GrammarCorrection;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.database.services.GrammarCorrectionService;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bvn13 on 03.02.2018.
 */
@Component
public class GrammarCorrectorListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?correct";

    private static SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Autowired
    private GrammarCorrectionService grammarCorrectionService;


    @Override
    public void onMessage(final MessageEvent event) throws Exception {

        super.onMessage(event);

        if (!channelSettingsService.getChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), this.getChannelName(event)).getGrammarCorrectionEnabled()) {
            return;
        }

        if (event.getUser() != null && event.getBot().getUserBot().getNick().equals(event.getUser().getNick())) {
            return;
        }

//        if (event.getMessage().startsWith(COMMAND)) {
//            onCommand(event);
//        } else {
            checkForCorrection(event);
//        }

    }

    private void checkForCorrection(final GenericMessageEvent event) throws Exception {
        String message = event.getMessage().replace(COMMAND, "").trim();
        HashMap<String, String[]> corrections = grammarCorrectionService.getCorrectionsForMessage(message);
        corrections.forEach((correction, data) -> {
            Pattern REGEX = Pattern.compile("(\\w*"+data[1]+"\\w*)", Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE);
//            String words[] = data[0].split("\\s");
//            for (String w : words) {
                Matcher matcher = REGEX.matcher(data[0].trim());
                if (matcher.find() && !matcher.group().trim().equalsIgnoreCase(data[2].trim())) {
                    String correct = matcher.group().replace(data[1], data[2]);
                    event.respond("*"+correct);
                }
            //}
        });
    }



    private void onCommand(final GenericMessageEvent event) throws Exception {
        String message = event.getMessage().replace(COMMAND, "").trim();
        String commands[] = message.split(" ", 2);
        if (commands.length == 2) {
            if (commands[0].trim().equalsIgnoreCase("add")) {
                String params[] = commands[1].trim().split(">");
                if (params.length != 2) {
                    event.respond(helpMessage());
                } else {
                    grammarCorrectionService.saveGrammarCorrection(params[0].trim(), params[1].trim(), event.getUser().getNick());
                    event.respond("added correction: "+params[0].trim()+" > "+params[1].trim());
                }
            } else if (commands[0].trim().equalsIgnoreCase("remove")) {
                String params[] = commands[1].trim().split(">");
                if (params.length == 1) {
                    // by word
                    if (grammarCorrectionService.removeAllCorrectionsByWord(commands[1].trim())) {
                        event.respond("all corrections by word "+commands[1].trim()+" were removed");
                    } else {
                        event.respond("corrections by word "+commands[1].trim()+" not found");
                    }
                } else {
                    // by correction
                    if (grammarCorrectionService.removeCorrection(params[0].trim(), params[1].trim())) {
                        event.respond("removed correction: "+params[0].trim()+" > "+params[1].trim());
                    } else {
                        event.respond("correction not found: "+params[0].trim()+" > "+params[1].trim());
                    }
                }

            } else {
                event.respond(helpMessage());
            }
        } else if (commands.length == 1) {
            if (commands[0].trim().equalsIgnoreCase("help")) {
                event.respond(helpMessage());
            } else if (commands[0].trim().equalsIgnoreCase("show")) {
                List<GrammarCorrection> corrections = grammarCorrectionService.getAllCorrections();
                if (corrections.size() > 0) {
                    event.respond("sent in private");
                    AtomicReference<Integer> i = new AtomicReference<>(0);
                    corrections.forEach(c -> {
                        i.set(i.get()+1);
                        event.respondPrivateMessage(""+i.get()+": "+c.getWord()+" > "+c.getCorrection()+" / by "+c.getAuthor()+" at "+dt.format(c.getDtUpdated()));
                    });
                } else {
                    event.respond("correction table is empty");
                }
            }
        } else {
            event.respondWith(helpMessage());
        }

    }

    private String helpMessage() {
        return "syntax: ?correct add <REGEX-formatted word> > <full correction> | ?correct remove <REGEX-formatted word> > <full correction> | ?correct show";
    }

}
