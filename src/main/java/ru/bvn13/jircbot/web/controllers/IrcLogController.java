package ru.bvn13.jircbot.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bvn13.jircbot.database.entities.IrcMessage;
import ru.bvn13.jircbot.database.services.IrcMessageService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Controller
@RequestMapping("/logs")
public class IrcLogController {

    private final SimpleDateFormat DATE_READER = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat DATE_WRITER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Autowired
    private IrcMessageService ircMessageService;



    @RequestMapping(value = "/{serverHost:.+}/{day}", method = RequestMethod.GET)
    public String index(@PathVariable String serverHost, @RequestParam(name = "channel") String channelName, @PathVariable String day, Model model) {
        model.addAttribute("serverHost", serverHost);
        model.addAttribute("channelName", channelName);
        model.addAttribute("day", day);
        return "irclog";
    }

    @RequestMapping(value = "/text/{serverHost:.+}/{day}", method = RequestMethod.GET, produces = "plain/text; charset=utf-8")
    public @ResponseBody String indexText(@PathVariable String serverHost, @RequestParam(name = "channel") String channelName, @PathVariable String day, Model model) throws Exception {
        StringBuilder sb = new StringBuilder();

        Date date = null;
        try {
            date = DATE_READER.parse(day);
        } catch (ParseException e) {
            throw new Exception("unknown date");
        }

        List<IrcMessage> messages = ircMessageService.getMessagesOfDay(serverHost, channelName, date);

        sb.append("SERVER: "+serverHost+"\n");
        sb.append("CHANNEL: "+channelName+"\n");
        sb.append("DATE: "+day+"\n");

        sb.append("\n");

        messages.forEach(msg -> {
            sb.append(DATE_WRITER.format(msg.getDtCreated())+" | ");
            if (msg.getUsername() != null && !msg.getUsername().isEmpty()) {
                sb.append(msg.getUsername()+" | ");
            }
            sb.append(msg.getMessage()+"\n");
        });

        return sb.toString();
    }




}
