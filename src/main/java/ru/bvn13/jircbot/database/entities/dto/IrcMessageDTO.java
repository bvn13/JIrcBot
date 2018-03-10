package ru.bvn13.jircbot.database.entities.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Getter
@Setter
public class IrcMessageDTO extends BaseDTO {

    private String serverHost;
    private String channelName;
    private String username;
    private String message;

    public boolean isUserSet() {
        return username != null && !username.isEmpty();
    }

}
