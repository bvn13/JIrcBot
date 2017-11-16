package ru.bvn13.jircbot.bot;


import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.listeners.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class JircBot extends ListenerAdapter {

    private static Logger logger = LoggerFactory.getLogger(JircBot.class);


    private JircBotConfiguration config;

    private Map<String, PircBotX> bots = new HashMap<>();


    @Autowired
    public JircBot(JircBotConfiguration config) {
        this.config = config;
        this.start();
    }


    public void start() {

        //Setup this bot
        Configuration.Builder templateConfig = new Configuration.Builder()
                .setLogin("JIrcBot") //login part of hostmask, eg name:login@host
                .setAutoNickChange(true) //Automatically change nick when the current one is in use
                .setCapEnabled(true) //Enable CAP features
                .addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true));

        this.config.getConnections().forEach(c -> {
            List<Configuration.ServerEntry> servers = new ArrayList<>();
            servers.add(new Configuration.ServerEntry(c.getServer(), c.getPort()));
            this.bots.put(
                    String.format("%s/%s", c.getServer(), c.getChannelName()),
                    new PircBotX(templateConfig
                            .setName(c.getBotName())
                            .addListener(new PingPongListener())
                            .addListener(new CalculatorListener()) //This class is a listener, so add it to the bots known listeners
                            .addListener(new GoogleDoodleListener(this.config))
                            .addListener(new GoogleSearchListener(this.config))
                            .addListener(new UrlRetrieverListener())
                            .addListener(new RegexCheckerListener())
                            .setServers(servers)
                            .setAutoReconnect(true)
                            .addAutoJoinChannel(c.getChannelName()) //Join the official #pircbotx channel
                            .buildForServer(c.getServer())
                    )
            );
        });

        //bot.connect throws various exceptions for failures
        this.bots.forEach((id, b) -> {
            try {
                b.startBot();
            } catch (Exception ex) {
                logger.error("ERROR STARTING BOT: "+id);
                ex.printStackTrace();
            }
        });

    }



}
