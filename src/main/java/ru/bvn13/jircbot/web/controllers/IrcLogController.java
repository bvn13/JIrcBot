package ru.bvn13.jircbot.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.bvn13.jircbot.database.entities.IrcMessage;
import ru.bvn13.jircbot.database.entities.dto.IrcMessageDTO;
import ru.bvn13.jircbot.database.services.IrcMessageService;
import ru.bvn13.jircbot.utilities.DTOUtil;
import ru.bvn13.jircbot.utilities.DateTimeUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Controller
@RequestMapping("/logs")
public class IrcLogController {

    private final SimpleDateFormat DATE_READER = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat DATE_WRITER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private IrcMessageService ircMessageService;



    @RequestMapping(value = "/{serverHost:.+}/{channelName:.+}/{day}", method = RequestMethod.GET)
    public String index(@PathVariable String serverHost, @PathVariable String channelName, @PathVariable String day, Model model) throws Exception {
        prepareModel(model, serverHost, channelName, day);
        return "irclog";
    }

    @RequestMapping(value = "/{serverHost:.+}/{channelName:.+}", method = RequestMethod.GET)
    public String index(@PathVariable String serverHost, @PathVariable String channelName, Model model) throws Exception {
        prepareModel(model, serverHost, channelName, DATE_READER.format(new Date()));
        return "irclog";
    }

    @RequestMapping(value = "/text/{serverHost:.+}/{channelName:.+}/{day}", method = RequestMethod.GET, produces = "plain/text; charset=utf-8")
    public @ResponseBody String indexText(@PathVariable String serverHost, @PathVariable String channelName, @PathVariable String day, Model model) throws Exception {
        return renderAsText(serverHost, channelName, day);
    }

    @RequestMapping(value = "/text/{serverHost:.+}/{channelName:.+}", method = RequestMethod.GET, produces = "plain/text; charset=utf-8")
    public @ResponseBody String indexText(@PathVariable String serverHost, @PathVariable String channelName, Model model) throws Exception {
        return renderAsText(serverHost, channelName, DATE_READER.format(new Date()));
    }


    private void prepareModel(Model model, String serverHost, String channelName, String day) throws Exception {
        model.addAttribute("serverHost", serverHost);
        model.addAttribute("day", day);

        Date date = null;
        try {
            date = DATE_READER.parse(day);
        } catch (ParseException e) {
            throw new Exception("unknown date");
        }

        LocalDateTime paramDay = DateTimeUtility.dateToLocalDateTime(date);
        Date prevDay = DateTimeUtility.localDateTimeToDate(paramDay.minusDays(1));
        Date nextDay = DateTimeUtility.localDateTimeToDate(paramDay.plusDays(1));

        model.addAttribute("prevDay", DATE_READER.format(prevDay));
        model.addAttribute("nextDay", DATE_READER.format(nextDay));

        List<IrcMessage> messages = ircMessageService.getMessagesOfDay(serverHost, channelName, date);
        if (messages.size() == 0) {
            model.addAttribute("channelNameStr", channelName);
        } else {
            model.addAttribute("channelNameStr", messages.get(0).getChannelName());
        }
        model.addAttribute("channelName", channelName);

        List<IrcMessageDTO> dtos = new ArrayList<>();
        messages.forEach(message -> {
            dtos.add(DTOUtil.map(message, IrcMessageDTO.class));
        });

        model.addAttribute("messages", dtos);
    }

    private String renderAsText(String serverHost, String channelName, String day) throws Exception {
        StringBuilder sb = new StringBuilder();

        Date date = null;
        try {
            date = DATE_READER.parse(day);
        } catch (ParseException e) {
            throw new Exception("unknown date");
        }

        List<IrcMessage> messages = ircMessageService.getMessagesOfDay(serverHost, channelName, date);

        sb.append("SERVER: "+serverHost+"\n");
        if (messages.size() == 0) {
            sb.append("CHANNEL: "+channelName+"\n");
        } else {
            sb.append("CHANNEL: "+messages.get(0).getChannelName()+"\n");
        }
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
