package ru.bvn13.jircbot.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Controller
@RequestMapping("/logs")
public class IrcLogController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index() {
        return "irclog";
    }

}
