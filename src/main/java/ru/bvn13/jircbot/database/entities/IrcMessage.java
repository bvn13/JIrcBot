package ru.bvn13.jircbot.database.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Entity
@Table(name = "irc_messages")
@Getter @Setter
public class IrcMessage extends BaseModel {

    @Column
    private String channelName;

    @Column
    private String username;

    @Column
    private String message;


    public IrcMessage(String channelName, String username, String message) {
        this.channelName = channelName;
        this.username = username;
        this.message = message;
    }

    public IrcMessage(String channelName, String message) {
        this.channelName = channelName;
        this.message = message;
    }

    public IrcMessage(String message) {
        this.message = message;
    }

}
