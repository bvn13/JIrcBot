package ru.bvn13.jircbot.bot;


import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.services.YandexSearchService;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.listeners.*;
import ru.bvn13.jircbot.listeners.advices.AdviceListener;
import ru.bvn13.jircbot.listeners.calculator.CalculatorListener;
import ru.bvn13.jircbot.listeners.quiz.QuizListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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


    private ScheduledExecutorService executorService;

    MultiBotManager manager = new MultiBotManager();

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
    private DeferredMessagesListener deferredMessagesListener;

    @Autowired
    private LinkPreviewListener linkPreviewListener;
    @Autowired
    private HelloOnJoinListener helloOnJoinListener;

    @Autowired
    private GrammarCorrectorListener grammarCorrectorListener;

    @Autowired
    private GoogleSearchListener googleSearchListener;

    @Autowired
    private LoggerListener loggerListener;

    @Autowired
    private AdminListener adminListener;

    @PostConstruct
    public void postConstruct() {
        this.executorService = Executors.newScheduledThreadPool(10);
        this.executorService.schedule(new Runnable() {
            @Override
            public void run() {
                initBots();
                startBots();
            }
        }, 5, TimeUnit.SECONDS);
        this.executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkBots();
            }
        }, 30*1000, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void preDestroy() {
        this.executorService.shutdown();
    }


    private void initBots() {
        logger.info(">>>>>>>>>>>>>>>>>>>> BOT INIT <<<<<<<<<<<<<<<<<<<<");

        //Setup this bot
        Configuration.Builder templateConfig = new Configuration.Builder()
                .setLogin("JIrcBot") //login part of hostmask, eg name:login@host
                .setAutoNickChange(true) //Automatically change nick when the current one is in use
                .setCapEnabled(true) //Enable CAP features
                .addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true));

        this.config.getConnections().forEach(c -> {
            List<Configuration.ServerEntry> servers = new ArrayList<>();
            servers.add(new Configuration.ServerEntry(c.getServer(), c.getPort()));

            Configuration.Builder confBuilder = templateConfig
                    .setName(c.getBotName())
                    .addListener(adminListener)
                    .addListener(pingPongListener)
                    .addListener(calculatorListener)
                    .addListener(regexCheckerListener)
                    .addListener(adviceListener)
                    .addListener(quizListener)
                    .addListener(bashOrgListener)
                    .addListener(autoRejoinListener)
                    .addListener(deferredMessagesListener)
                    .addListener(linkPreviewListener)
                    .addListener(helloOnJoinListener)
                    .addListener(grammarCorrectorListener)
                    .addListener(googleSearchListener)
                    .addListener(loggerListener)

                    // not tested
                    //.addListener(new GoogleDoodleListener(this.config))
                    //.addListener(new YandexSearchListener(this.config, this.yandexSearchService))

                    .setServers(servers)
                    .setAutoReconnect(true)
                    .addAutoJoinChannels(c.getChannelsNames());

            if (c.getBotPassword() != null && !c.getBotPassword().isEmpty()) {
                confBuilder.setNickservPassword(c.getBotPassword());
            }

            this.bots.put(
                    String.format("%s/%s", c.getServer(), "1"),
                            new PircBotX(confBuilder.buildForServer(c.getServer())
                    )
            );
        });
    }


    private void startBots() {
        logger.info(">>>>>>>>>>>>>>>>>>>> BOT STARTING <<<<<<<<<<<<<<<<<<<<");

        this.bots.forEach((id, bot) -> {
            manager.addBot(bot);
        });

        manager.start();
    }

    private void checkBots() {
        this.manager.getBots().forEach(bot -> {
            if (bot.getState().equals(PircBotX.State.DISCONNECTED)) {
                try {
                    bot.startBot();
                } catch (IOException e) {
                    logger.error("Could not start bot at "+bot.getUserBot().getServer(), e);
                } catch (IrcException e) {
                    logger.error("IrcException while starting bot at "+bot.getUserBot().getServer(), e);
                }
            }
        });
    }

    public static String extractServer(String s) {
        String d[] = s.split("[\\.]");
        return ""+d[d.length-2]+"."+d[d.length-1];
    }

}
