package ru.bvn13.jircbot.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
//@Getter
//@Setter
public class Config {

    private Boolean enabled = false;

    private String server;

    private Integer port = 6667;

    private String botName;

    private String botPassword = "";

    private List<String> channelsNames;

    private String masterNick;

}
