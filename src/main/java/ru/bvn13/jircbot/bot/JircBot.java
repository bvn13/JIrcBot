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
import ru.bvn13.jircbot.Services.YandexSearchService;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.listeners.*;
import ru.bvn13.jircbot.listeners.advices.AdviceListener;
import ru.bvn13.jircbot.listeners.calculator.CalculatorListener;
import ru.bvn13.jircbot.listeners.quiz.QuizListener;

import javax.annotation.PostConstruct;
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
    private YandexSearchService yandexSearchService;


    @Autowired
    public JircBot(JircBotConfiguration config) {
        this.config = config;
    }


    @Autowired
    private PingPongListener pingPongListener;
    @Autowired
    private CalculatorListener calculatorListener;
    @Autowired
    private RegexCheckerListener regexCheckerListener;
    @Autowired
    private AdviceListener adviceListener;
    @Autowired
    private QuizListener quizListener;
    @Autowired
    private BashOrgListener bashOrgListener;
    @Autowired
    private AutoRejoinListener autoRejoinListener;

    @Autowired
    private LinkPreviewListener linkPreviewListener;
    @Autowired
    private HelloOnJoinListener helloOnJoinListener;


    @PostConstruct
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
                    String.format("%s/%s", c.getServer(), "1"), //c.getChannelName()),
                    new PircBotX(templateConfig
                            .setName(c.getBotName())
                            .addListener(pingPongListener)
                            .addListener(calculatorListener)
                            .addListener(regexCheckerListener)
                            .addListener(adviceListener)
                            .addListener(quizListener)
                            .addListener(bashOrgListener)
                            .addListener(autoRejoinListener)

                            // disabled yet
                            //.addListener(linkPreviewListener)
                            //.addListener(helloOnJoinListener)

                            // not tested
                            //.addListener(new GoogleDoodleListener(this.config))
                            //.addListener(new GoogleSearchListener(this.config))
                            //.addListener(new YandexSearchListener(this.config, this.yandexSearchService))

                            .setServers(servers)
                            .setAutoReconnect(true)
                            .addAutoJoinChannels(c.getChannelsNames())
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
