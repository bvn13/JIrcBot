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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.config.JircBotConfiguration;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
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

    @Value("${jircbot.version}")
    private String version;
    private String getVersion() {
        return version == null ? "" : version;
    }

    private JircBotConfiguration config;
    private ChannelSettingsService channelSettingsService;

    private Map<String, PircBotX> bots = new HashMap<>();



    @Autowired
    public JircBot(JircBotConfiguration config, ChannelSettingsService channelSettingsService) {
        this.config = config;
        this.channelSettingsService = channelSettingsService;
    }


    private ScheduledExecutorService executorService;

    private MultiBotManager manager = new MultiBotManager();

    private PingPongListener pingPongListener;
    private CalculatorListener calculatorListener;
    private RegexCheckerListener regexCheckerListener;
    private AdviceListener adviceListener;
    private QuizListener quizListener;
    private BashOrgListener bashOrgListener;
    private AutoRejoinListener autoRejoinListener;
    private DeferredMessagesListener deferredMessagesListener;
    private LinkPreviewListener linkPreviewListener;
    private HelloOnJoinListener helloOnJoinListener;
    private GrammarCorrectorListener grammarCorrectorListener;
    private HelpListener helpListener;
    private DuckDuckGoSearchListener duckDuckGoSearchListener;
    private LoggerListener loggerListener;
    private AdminListener adminListener;
    private StatisticsListener statisticsListener;




    @PostConstruct
    public void postConstruct() {
        logger.warn("VERSION: "+version);
        this.executorService = Executors.newScheduledThreadPool(10);
        this.executorService.schedule(() -> {
            initBots();
            startBots();
        }, 5, TimeUnit.SECONDS);
        this.executorService.scheduleAtFixedRate(() -> {
            logger.debug("check");
            checkBots();
        }, 15, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void preDestroy() {
        logger.warn("Bot is shutting down...");
        this.executorService.shutdown();
        this.manager.stop("Bot is shutting down...");
    }


    private void initBots() {
        logger.info(">>>>>>>>>>>>>>>>>>>> BOT INIT : "+getVersion()+" <<<<<<<<<<<<<<<<<<<<");

        //Setup this bot
        Configuration.Builder templateConfig = new Configuration.Builder()
                .setLogin("JIrcBot") //login part of hostmask, eg name:login@host
                .setAutoNickChange(true) //Automatically change nick when the current one is in use
                .setCapEnabled(true) //Enable CAP features
//                .addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true))
                ;

        this.config.getConnections().forEach(c -> {
            List<Configuration.ServerEntry> servers = new ArrayList<>();
            servers.add(new Configuration.ServerEntry(c.getServer(), c.getPort()));

            Configuration.Builder confBuilder = templateConfig
                    .setRealName("JIrcBot v"+getVersion()+" | github.com/bvn13/JIrcBot")
                    .setName(c.getBotName())
                    .addListener(adminListener)
                    .addListener(statisticsListener)
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
                    .addListener(helpListener)
                    .addListener(duckDuckGoSearchListener)
                    .addListener(loggerListener)

                    .setServers(servers)
                    .setAutoReconnect(true)
                    .addAutoJoinChannels(channelSettingsService.getListeningChannels(c.getServer(), c.getChannelsNames()));

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

        if (this.manager.getBots().size() < this.bots.size()) {
            logger.warn("CHECKING BOTS");
            logger.debug("BOTS COUNT: " + this.manager.getBots().size());
        }

        this.bots.forEach((id, bot) -> {
            if (!this.manager.getBots().contains(bot)) {
                logger.warn("RECONNECTION BOT "+bot.getServerHostname());
                bot.stopBotReconnect();
                this.manager.addBot(bot);
            } else {
                if (bot.getState().equals(PircBotX.State.DISCONNECTED)) {
                    logger.warn("RECONNECTION BOT "+bot.getServerHostname());
                    bot.stopBotReconnect();
                }
            }
        });

    }

    private void startBot(PircBotX bot) {
        logger.warn("BOT "+bot.getServerHostname()+" STATUS "+bot.getState().toString());
        try {
            bot.startBot();
        } catch (IOException e) {
            logger.error("Could not start bot at "+bot.getServerHostname(), e);
        } catch (IrcException e) {
            logger.error("IrcException while starting bot at "+bot.getServerHostname(), e);
        }
    }

    public static String extractServer(String s) {
        String d[] = s.split("[\\.]");
        return ""+d[d.length-2]+"."+d[d.length-1];
    }

    //*************************************************

    @Autowired
    public void setPingPongListener(PingPongListener pingPongListener) {
        this.pingPongListener = pingPongListener;
    }

    @Autowired
    public void setCalculatorListener(CalculatorListener calculatorListener) {
        this.calculatorListener = calculatorListener;
    }

    @Autowired
    public void setRegexCheckerListener(RegexCheckerListener regexCheckerListener) {
        this.regexCheckerListener = regexCheckerListener;
    }

    @Autowired
    public void setAdviceListener(AdviceListener adviceListener) {
        this.adviceListener = adviceListener;
    }

    @Autowired
    public void setQuizListener(QuizListener quizListener) {
        this.quizListener = quizListener;
    }

    @Autowired
    public void setBashOrgListener(BashOrgListener bashOrgListener) {
        this.bashOrgListener = bashOrgListener;
    }

    @Autowired
    public void setAutoRejoinListener(AutoRejoinListener autoRejoinListener) {
        this.autoRejoinListener = autoRejoinListener;
    }

    @Autowired
    public void setDeferredMessagesListener(DeferredMessagesListener deferredMessagesListener) {
        this.deferredMessagesListener = deferredMessagesListener;
    }

    @Autowired
    public void setLinkPreviewListener(LinkPreviewListener linkPreviewListener) {
        this.linkPreviewListener = linkPreviewListener;
    }

    @Autowired
    public void setHelloOnJoinListener(HelloOnJoinListener helloOnJoinListener) {
        this.helloOnJoinListener = helloOnJoinListener;
    }

    @Autowired
    public void setGrammarCorrectorListener(GrammarCorrectorListener grammarCorrectorListener) {
        this.grammarCorrectorListener = grammarCorrectorListener;
    }

    @Autowired
    public void setHelpListener(HelpListener helpListener) {
        this.helpListener = helpListener;
    }

    @Autowired
    public void setDuckDuckGoSearchListener(DuckDuckGoSearchListener duckDuckGoSearchListener) {
        this.duckDuckGoSearchListener = duckDuckGoSearchListener;
    }

    @Autowired
    public void setLoggerListener(LoggerListener loggerListener) {
        this.loggerListener = loggerListener;
    }

    @Autowired
    public void setAdminListener(AdminListener adminListener) {
        this.adminListener = adminListener;
    }

    @Autowired
    public void setStatisticsListener(StatisticsListener statisticsListener) {
        this.statisticsListener = statisticsListener;
    }
}
