package ru.bvn13.jircbot.model;

import lombok.Data;

@Data
public class YandexSearchSettings implements ListenerSettings {

    private String url;

    private String user;

    private String key;

}
