package ru.bvn13.jircbot.model;

import lombok.Data;

import java.util.List;

@Data
public class Config {

    private Boolean enabled = false;

    private String server;

    private Integer port = 6667;

    private String botName;

    private List<String> channelsNames;

}
