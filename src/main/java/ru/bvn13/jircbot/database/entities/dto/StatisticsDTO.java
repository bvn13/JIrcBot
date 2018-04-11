package ru.bvn13.jircbot.database.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by bvn13 on 11.04.2018.
 */
@Getter @Setter
@AllArgsConstructor
public class StatisticsDTO {

    private String username;
    private Integer count;

}
