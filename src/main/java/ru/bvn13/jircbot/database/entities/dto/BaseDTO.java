package ru.bvn13.jircbot.database.entities.dto;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Getter
@Setter
public abstract class BaseDTO {

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private Long id;
    private Date dtCreated;
    private Date dtUpdated;

    public String getDtCreatedStr() {
        return DATE_FORMATTER.format(dtCreated);
    }

}
