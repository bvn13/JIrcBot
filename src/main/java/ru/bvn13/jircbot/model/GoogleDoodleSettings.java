package ru.bvn13.jircbot.model;

import lombok.Data;

@Data
public class GoogleDoodleSettings implements ListenerSettings {

    private String mainUrl;
    private String checkUrl;
    private String linkSelector;
    private String titleSelector;
    private String dateSelector;

}
