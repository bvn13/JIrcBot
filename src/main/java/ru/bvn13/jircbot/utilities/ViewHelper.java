package ru.bvn13.jircbot.utilities;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Created by bvn13 on 22.03.2018.
 */
@Component
public class ViewHelper {

    public String getColorForString(String s) {
        Random r = new Random(s.hashCode());
        return String.format("#%06x", r.nextInt(256*256*256));
    }

    public String getColorStyleForUser(String username) {
        return String.format("color: %s;", getColorForString(username));
    }

}
