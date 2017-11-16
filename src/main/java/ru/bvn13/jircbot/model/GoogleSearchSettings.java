package ru.bvn13.jircbot.model;

import lombok.Data;

@Data
public class GoogleSearchSettings implements ListenerSettings {

    private String uuid;
    private String appKey;

}
