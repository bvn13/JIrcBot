package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.database.entities.ChannelSettings;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.documentation.DescriptionProvided;
import ru.bvn13.jircbot.documentation.DocumentationProvider;
import ru.bvn13.jircbot.documentation.ListenerDescription;
import ru.bvn13.jircbot.model.Config;

import java.util.concurrent.atomic.AtomicReference;

import static ru.bvn13.jircbot.documentation.ListenerDescription.CommandDescription;

/**
 * Created by bvn13 on 27.03.2018.
 */
@Component
public class AdminListener extends ImprovedListenerAdapter implements DescriptionProvided {

    private static final Logger logger = LoggerFactory.getLogger(AdminListener.class);

    private static final String COMMAND = "?";

    private JircBotConfiguration configuration;

    private ChannelSettingsService channelSettingsService;

    @Autowired
    public AdminListener(DocumentationProvider documentationProvider) {
        this.registerDescription(documentationProvider);
    }

    @Override
    public ListenerDescription getDescription() {
        return ListenerDescription.create()
                .setModuleName("AdminListener")
                .setModuleDescription("Admin commands of the bot\nMust be send in private dialog\nYou must be the owner of the bot (config.json) and your nick must be registered and verified at server")
                .addCommand(CommandDescription.builder()
                        .command("join")
                        .description("joins given channel on current server")
                        .example("?join [#CHANNEL_NAME]")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("leave")
                        .description("joins given channel on current server")
                        .example("?leave [#CHANNEL_NAME]")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("restart")
                        .description("trying to restart the bot on current server")
                        .example("?restart")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("privmsg")
                        .description("send private message")
                        .example("?privmsg [YOUR_FRIEND_NICK]")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("cmd")
                        .description("send RAW command to server (be careful with that shit!)")
                        .example("?cmd [ANY COMMAND WITH PARAMS]")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("op|deop")
                        .description("give / take away OP user status")
                        .example("?op [#CHANNEL_NAME] [YOUR_FRIEND_NICK]\n?deop [#CHANNEL_NAME] [YOUR_HOSTILE_NICK]")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("kick")
                        .description("kick user from channel")
                        .example("?kick [#CHANNEL_NAME] [YOUR_HOSTILE_NICK]")
                        .build()
                )
                .addCommand(CommandDescription.builder()
                        .command("set")
                        .description("set ON|OFF any of bot opportunity for channel\n\n"+
                                "Opportunities: \n\n"+
                                "autorejoin | auto-rejoin - auto rejoin channel on kicking\n"+
                                "bash | bashorg - bach.org quoting\n"+
                                "deferredmessages | deferred-messages | tell - saving and delivering deferred messages\n"+
                                "s | search - search in DuckDuckGo for you\n"+
                                "grammar | grammarcorrection | grammar-correction - grammar checker (does not work in current version)\n"+
                                "hello | helloonjoin | hello-on-join - sending your phrase on user joins\n"+
                                "links | linkpreview | links-preview - sending titles of web pages by given URL\n"+
                                "logging | log - logging for channel\n"+
                                "regex | regexp | regexchecker | regexpchecker | regex-checker | regexp-checker - regular expression checker\n"+
                                "advice | advices - give your friend advices\n"+
                                "calc | calculator - calculator\n"+
                                "quiz - quiz\n")
                        .example("?set [#CHANNEL_NAME] [OPPORTUNITY] [ON|OFF]")
                        .build()
                )
                ;
    }


    @Override
    public void onJoin(JoinEvent event) throws Exception {
        super.onJoin(event);

        if (event.getChannel().getName().startsWith("#")) {
            if (event.getUser() != null && event.getBot().getNick().equalsIgnoreCase(event.getUser().getNick())) {
                event.getBot().sendRaw().rawLineNow("MODE " + event.getBot().getUserBot().getNick() + " +B");
                try {
                    channelSettingsService.createChannelSettings(JircBot.extractServer(event.getBot().getServerHostname()), event.getChannel().getName());
                } catch (Exception e) {
                    logger.error("Could not create channel settings for channel "+event.getChannel().getName()+" at "+event.getBot().getServerHostname(), e);
                }
            }
        }
    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);

        Config config = getBotConfig(event);
        if (config == null) {
            return;
        }

        if (event.getUser() != null && event.getUser().isVerified()
                && !config.getMasterNick().isEmpty()
                && config.getMasterNick().equals(event.getUser().getNick())) {

            if (event.getMessage().startsWith(COMMAND)) {
                String command = event.getMessage().substring(COMMAND.length());
                String[] commands = command.trim().split(" ", 2);

                if (commands[0].startsWith("+") || commands[0].startsWith("-")) {
                    boolean isApply = commands[0].startsWith("+");
                    command = commands[0].substring(1);

                    switch (command.toLowerCase()) {
                        case "op" :
                            if (commands.length == 1) {
                                event.getBot().sendRaw().rawLine("PRIVMSG chanserv :" + (!isApply ? "deop" : "op") + " " + event.getChannel().getName() + " " + event.getUser().getNick());
                            } else {
                                event.getBot().sendRaw().rawLine("PRIVMSG chanserv :" + (!isApply ? "deop" : "op") + " " + event.getChannel().getName() + " " + commands[1]);
                            }
                            break;
                        case "v" :
                        case "voice":
                            if (commands.length == 1) {
                                event.getBot().sendRaw().rawLine("MODE " + event.getChannel().getName() + " " + (isApply ? "+" : "-") + "v " + event.getUser().getNick());
                            } else {
                                event.getBot().sendRaw().rawLine("MODE " + event.getChannel().getName() + " " + (isApply ? "+" : "-") + "v " + commands[1]);
                            }
                            break;
                        default:
                            event.respond("Wrong command. Options: ?+ op|v[oice]");
                            break;
                    }
                } else {
                    switch (command.toLowerCase()) {
                        case "inv" :
                        case "invite" :
                            if (command.length() > 1) {
                                event.getBot().sendRaw().rawLine("INVITE " + event.getChannel().getName() + " " + commands[1]);
                            }
                            break;
                        case "kick" :
                            if (command.length() > 1) {
                                event.getBot().sendRaw().rawLine("KICK " + event.getChannel().getName() + " " + commands[1] + (commands.length > 2 ? " "+commands[2] : ""));
                            }
                            break;
                        default:
                            //event.respond("Wrong command. Try to send it privately.");
                            break;
                    }
                }

            }

        }
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent event) throws Exception {

        super.onPrivateMessage(event);

        Config config = getBotConfig(event);
        if (config == null) {
            return;
        }

        if (event.getUser() != null && event.getUser().isVerified()
                && !config.getMasterNick().isEmpty()
                && config.getMasterNick().equals(event.getUser().getNick())) {

            if (event.getMessage().startsWith(COMMAND)) {
                String command = event.getMessage().substring(COMMAND.length());
                String[] commands = command.trim().split(" ", 2);

                if (commands.length != 2) {
                    event.respondPrivateMessage("Wrong command");
                    return;
                }

                String[] args = null;
                switch (commands[0].toLowerCase()) {
                    case "restart":
                        event.getBot().stopBotReconnect(); break;
                    case "join" :
                        event.getBot().sendIRC().joinChannel(commands[1]); event.respondPrivateMessage("done"); break;
                    case "leave" :
                        event.getBot().sendRaw().rawLine("PART "+commands[1]); event.respondPrivateMessage("done"); break;
                    case "privmsg" :
                        event.getBot().sendRaw().rawLine("PRIVMSG "+commands[1]); event.respondPrivateMessage("done"); break;
                    case "cmd" :
                        //args = commands[1].split(" ", 2);
                        event.getBot().sendRaw().rawLine(commands[1]); event.respondPrivateMessage("done"); break;
                    case "set" :
                        try {
                            args = commands[1].split(" ", 3); // set, channel, mode/hello-message
                            if (args.length == 3) {
                                changeSettings(JircBot.extractServer(event.getBot().getServerHostname()), args[0], args[1], args[2]); //server, channel, settings, mode
                                event.respondPrivateMessage("done");
                            } else {
                                event.respondPrivateMessage("SYNTAX: ?set <channel> <option> on/off");
                            }
                        } catch (Exception e) {
                            event.respondPrivateMessage(e.toString());
                            logger.error("ERROR", e);
                        }
                        break;
                    case "op" :
                    case "deop" :
                        String cmd = commands[0].toLowerCase();
                        args = commands[1].split(" ", 2);
                        if (args.length == 1) {
                            event.getBot().sendRaw().rawLine("PRIVMSG chanserv :"+cmd+" "+args[0]+" "+event.getUser().getNick());
                            event.respondPrivateMessage("done");
                        } else if (args.length == 2) {
                            event.getBot().sendRaw().rawLine("PRIVMSG chanserv :"+cmd+" "+args[0]+" "+args[1]);
                            event.respondPrivateMessage("done");
                        } else {
                            event.respondPrivateMessage("wrong arguments");
                        }
                        break;
                    case "kick" :
                        args = commands[1].split(" ", 3);
                        if (args.length == 2) {
                            event.getBot().sendRaw().rawLine("KICK "+args[0]+" "+args[1]);
                            event.respondPrivateMessage("done");
                        } else if (args.length == 3) {
                            event.getBot().sendRaw().rawLine("KICK "+args[0]+" "+args[1]+" "+args[2]);
                            event.respondPrivateMessage("done");
                        } else {
                            event.respondPrivateMessage("wrong arguments");
                        }
                        break;
                    default:
                        event.respondPrivateMessage("Command not supported");
                }

            }

        }

    }

    private boolean sameServer(String s1, String s2) {
        String[] d1 = s1.split("[\\.]");
        String[] d2 = s2.split("[\\.]");

        if (d1.length >= 2 && d2.length >= 2) {
            if (d1[d1.length - 1].equals(d2[d2.length - 1])
                    && d1[d1.length - 2].equals(d2[d2.length - 2])) {
                return true;
            }
        }

        return false;
    }

    private Config getBotConfig(GenericMessageEvent event) {
        AtomicReference<Config> aConfig = new AtomicReference<>(null);

        configuration.getConnections().forEach(c -> {

            if (sameServer(event.getBot().getServerHostname(), c.getServer())) {
                aConfig.set(c);
            }

        });

        if (aConfig.get() == null) {
            event.respondPrivateMessage("sorry, bot not found!");
            return null;
        }

        return aConfig.get();
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
                case "deferredmessages":
                case "deferred-messages":
                case "tell" :
                    settings.setDeferredMessagesEnabled(mode);
                    break;
                case "s":
                case "search":
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

    @Autowired
    public void setConfiguration(JircBotConfiguration configuration) {
        this.configuration = configuration;
    }

    @Autowired
    public void setChannelSettingsService(ChannelSettingsService channelSettingsService) {
        this.channelSettingsService = channelSettingsService;
    }
}
