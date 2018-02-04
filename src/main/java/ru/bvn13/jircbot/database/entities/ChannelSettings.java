package ru.bvn13.jircbot.database.entities;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Entity
@Table(name = "channel_settings", uniqueConstraints = {@UniqueConstraint(columnNames = {"channelName"}, name = "uniq_channel_settings_channel_name")})
public class ChannelSettings extends BaseModel {

    @Getter
    @Setter
    @Column(nullable = false)
    private String channelName;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean calculatorEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean regexCheckerEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean advicesEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean quizEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean bashOrgEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean autoRejoinEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean linkPreviewEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean helloOnJoinEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean deferredMessagesEnabled = false;

    @Getter
    @Setter
    @Column(nullable = false, columnDefinition = "Boolean DEFAULT False")
    private Boolean grammarCorrectionEnabled = false;


}
