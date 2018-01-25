package ru.bvn13.jircbot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import ru.bvn13.jircbot.bot.JircBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("ru.bvn13.jircbot")
public class MainApp implements CommandLineRunner {

    @Autowired
    private JircBot bot;

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
        System.out.println("==============> STARTING <==============");
    }

    @Override
    public void run(String... strings) throws Exception {

    }
}
