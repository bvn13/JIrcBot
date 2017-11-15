package ru.bvn13.jircbot.model;

import com.sun.istack.internal.NotNull;
import lombok.Data;

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

    private String channelName;

}
