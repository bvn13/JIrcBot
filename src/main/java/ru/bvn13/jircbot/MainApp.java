package ru.bvn13.jircbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import ru.bvn13.jircbot.bot.JircBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration//(exclude={DataSourceAutoConfiguration.class})
@ComponentScan("ru.bvn13.jircbot")
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    @Autowired
    private JircBot bot;

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
        logger.info("==============> STARTING <==============");
    }

}
