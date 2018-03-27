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
@Table(name = "channel_settings", uniqueConstraints = {@UniqueConstraint(columnNames = {"serverHost", "channelName"}, name = "uniq_channel_settings_server_host_channel_name")})
@Getter @Setter
public class ChannelSettings extends BaseModel {

    @Column(nullable = false, columnDefinition = "VARCHAR(255) DEFAULT ''")
    private String serverHost;

    @Column(nullable = false)
    private String channelName;

    @Column(nullable = false)
    private Boolean calculatorEnabled = false;

    @Column(nullable = false)
    private Boolean regexCheckerEnabled = false;

    @Column(nullable = false)
    private Boolean advicesEnabled = false;

    @Column(nullable = false)
    private Boolean quizEnabled = false;

    @Column(nullable = false)
    private Boolean bashOrgEnabled = false;

    @Column(nullable = false)
    private Boolean autoRejoinEnabled = false;

    @Column(nullable = false)
    private Boolean linkPreviewEnabled = false;

    @Column(nullable = false)
    private Boolean helloOnJoinEnabled = false;

    @Column(nullable = false)
    private Boolean deferredMessagesEnabled = false;

    @Column(nullable = false, columnDefinition = "Boolean DEFAULT False")
    private Boolean grammarCorrectionEnabled = false;

    @Column(nullable = false, columnDefinition = "Boolean DEFAULT False")
    private Boolean googleSearchEnabled = false;

    @Column(nullable = false, columnDefinition = "Boolean DEFAULT False")
    private Boolean loggingEnabled = false;

    @Column(nullable = false, columnDefinition = "varchar(255) default ''")
    private String onJoinMessage = "";

}
