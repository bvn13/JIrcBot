package ru.bvn13.jircbot.database.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class IrcMessage extends BaseModel {

    @Column
    private String serverHost;

    @Column
    private String channelName;

    @Column
    private String username;

    @Column(columnDefinition = "TEXT")
    private String message;


    public IrcMessage(String serverHost, String channelName, String username, String message) {
        this.serverHost = serverHost;
        this.channelName = channelName;
        this.username = username;
        this.message = message;
    }

    public IrcMessage(String serverHost, String channelName, String message) {
        this.serverHost = serverHost;
        this.channelName = channelName;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("IrcMessage(%s at %s%s, msg=%s)", username, channelName, serverHost, message);
    }

}
