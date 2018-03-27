package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.database.entities.ChannelSettings;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.model.Config;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bvn13 on 27.03.2018.
 */
@Component
public class AdminListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?";

    @Autowired
    private JircBotConfiguration configuration;

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (event.getChannel().getName().startsWith("#")) {
            channelSettingsService.creaateChannelSettings(JircBot.extractServer(event.getUser().getServer()), event.getChannel().getName());
        }
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {

        AtomicReference<Config> aConfig = new AtomicReference<>(null);

        configuration.getConnections().forEach(c -> {

            if (sameServer(event.getUser().getServer(), c.getServer())) {
                aConfig.set(c);
            }

        });

        if (aConfig.get() == null) {
            return;
        }

        Config config = aConfig.get();

        if (event.getUser().isVerified()
                && !config.getMasterNick().isEmpty()
                && config.getMasterNick().equals(event.getUser().getNick())) {

            if (event.getMessage().startsWith(COMMAND)) {
                String command = event.getMessage().substring(COMMAND.length());
                String commands[] = command.trim().split(" ", 2);

                if (commands.length != 2) {
                    event.respondPrivateMessage("Wrong command");
                    return;
                }

                switch (commands[0].toLowerCase()) {
                    case "join" :
                        event.getBot().sendIRC().joinChannel(commands[1]); event.respondPrivateMessage("done"); break;
                    case "leave" :
                        event.getBot().sendRaw().rawLine("PART "+commands[1]); event.respondPrivateMessage("done"); break;
                    case "cmd" :
                        event.getBot().sendRaw().rawLine(command); event.respondPrivateMessage("done"); break;
                    case "set" :
                        try {
                            String args[] = commands[1].split(" ", 4); // set, server, channel, mode/hello-message
                            changeSettings(JircBot.extractServer(args[1]),  args[2], args[0], args[3]);
                            event.respondPrivateMessage("done");
                        } catch (Exception e) {
                            event.respondPrivateMessage(e.getMessage());
                        }
                        break;
                    default:
                        event.respondPrivateMessage("Command not supported");
                }

            }

        }

    }

    private boolean sameServer(String s1, String s2) {
        String d1[] = s1.split("[\\.]");
        String d2[] = s2.split("[\\.]");

        if (d1.length >= 2 && d2.length >= 2) {
            if (d1[d1.length - 1].equals(d2[d2.length - 1])
                    && d1[d1.length - 2].equals(d2[d2.length - 2])) {
                return true;
            }
        }

        return false;
    }



    private void changeSettings(String serverHost, String channelName, String set, String modeStr) {
        if (set.equals("hello-message") || set.equals("hello-msg")) {
            ChannelSettings settings = channelSettingsService.getChannelSettings(serverHost, channelName);
            settings.setOnJoinMessage(modeStr);
            channelSettingsService.save(settings);
        } else {
            if (!modeStr.equals("off") && !modeStr.equals("on")) {
                throw new RuntimeException("Wrong mode. Variants: on, off");
            }

            Boolean mode = modeStr.equals("on");
            ChannelSettings settings = channelSettingsService.getChannelSettings(serverHost, channelName);

            switch (set.toLowerCase()) {
                case "autorejoin":
                case "auto-rejoin":
                    settings.setAutoRejoinEnabled(mode);
                    break;
                case "bash":
                case "bashorg":
                    settings.setBashOrgEnabled(mode);
                    break;
                case "defferedmessages":
                case "deffered-messages":
                    settings.setDeferredMessagesEnabled(mode);
                    break;
                case "gs":
                case "googlesearch":
                case "google-search":
                    settings.setGoogleSearchEnabled(mode);
                    break;
                case "grammar":
                case "grammarcorrection":
                case "grammar-correction":
                    settings.setGrammarCorrectionEnabled(mode);
                    break;
                case "hello":
                case "helloonjoin":
                case "hello-on-join":
                    settings.setHelloOnJoinEnabled(mode);
                    break;
                case "links":
                case "linkpreview":
                case "links-preview":
                    settings.setLinkPreviewEnabled(mode);
                    break;
                case "logging":
                case "log":
                    settings.setLoggingEnabled(mode);
                    break;
                case "regex":
                case "regexp":
                case "regexchecker":
                case "regexpchecker":
                case "regex-checker":
                case "regexp-checker":
                    settings.setRegexCheckerEnabled(mode);
                    break;
                case "advice":
                case "advices":
                    settings.setAdvicesEnabled(mode);
                    break;
                case "calc":
                case "calculator":
                    settings.setCalculatorEnabled(mode);
                    break;
                case "quiz":
                    settings.setQuizEnabled(mode);
                    break;
                default:
                    throw new RuntimeException("Setting " + set + " not exist");
            }

            channelSettingsService.save(settings);
        }
    }
}
