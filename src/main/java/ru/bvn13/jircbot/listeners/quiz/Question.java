package ru.bvn13.jircbot.listeners.quiz;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn13 on 26.01.2018.
 */
@Data
public class Question {
    private String id;
    private String question;
    private List<String> answers = new ArrayList<>();

    private String correctAnswer;

    public String toString() {
        String s = ""+id+": "+question+"\nANSWERS: \n";
        for (String a : answers) {
            s += a+"\n";
        }
        s += "\nCORRECT ANSWER: "+correctAnswer;
        return s;
    }
}
