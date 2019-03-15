package ru.bvn13.jircbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
        logger.info("==============> STARTING <==============");
    }

}
