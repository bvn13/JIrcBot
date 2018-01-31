package ru.bvn13.jircbot.database.entities;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bvn13 on 31.01.2018.
 */
public class ChannelSettings extends BaseModel {

    @Getter
    @Setter
    private String channelName;

    @Getter
    @Setter
    private String botName;

    @Getter
    @Setter
    private Boolean pingPongEnabled;

    @Getter
    @Setter
    private Boolean calculatorEnabled;

    @Getter
    @Setter
    private Boolean regexCheckerEnabled;

    @Getter
    @Setter
    private Boolean adviceEnabled;

    @Getter
    @Setter
    private Boolean quizEnabled;

    @Getter
    @Setter
    private Boolean bashOrgEnabled;

    @Getter
    @Setter
    private Boolean autoRejoinEnabled;

}
