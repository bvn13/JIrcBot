package ru.bvn13.jircbot.model;

import com.sun.istack.internal.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class Config {

    @NotNull
    private Boolean enabled = false;

    @NotNull
    private String server;

    @NotNull
    private Integer port = 6667;

    @NotNull
    private String botName;

    private List<String> channelsNames;

}
