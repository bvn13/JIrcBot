package ru.bvn13.jircbot.database.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Entity
@Table(name = "deferred_messages")
public class DeferredMessage extends BaseModel {

    @Getter
    @Setter
    private String sender;

    @Getter
    @Setter
    private String recipient;

    @Getter
    @Setter
    @Type(type = "text")
    private String message;

    @Getter
    private Boolean sent;
    public Boolean isSent() {
        return sent;
    }
    public void setSent(Boolean sent) {
        this.sent = sent;
    }
}
