package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.events.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.bot.JircBot;
import ru.bvn13.jircbot.database.entities.dto.StatisticsDTO;
import ru.bvn13.jircbot.database.services.IrcMessageService;
import ru.bvn13.jircbot.utilities.DateTimeUtility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn13 on 11.04.2018.
 */
@Component
public class StatisticsListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?stats";

    private static class PeriodInfo {
        String period;
        Date dateStart;
    }

    @Autowired
    private IrcMessageService ircMessageService;

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);

        if (!event.getMessage().startsWith(COMMAND)) {
            return;
        }

        String commands[] = event.getMessage().toLowerCase().split(" ");

        if (commands.length > 3) {
            sendHelp(event);
            return;
        }

        List<StatisticsDTO> statistics = null;

        if (commands.length == 1) {

            statistics = ircMessageService.getStatistics(event.getBot().getServerHostname(), event.getChannel().getName());
            if (statistics.size() == 0) {
                event.respondChannel("Statistics is empty now");
                return;
            }
            event.respondChannel("TOP 5 for all over time:");

            for (int i = 0; i < 5 && i < statistics.size(); i++) {
                StatisticsDTO stats = statistics.get(i);
                event.respondChannel("" + (i + 1) + ". " + stats.getUsername() + " - " + stats.getCount());
            }

        } else if (commands.length == 2 && !commands[1].equals("all")) {

            PeriodInfo periodInfo = null;
            try {
                periodInfo = parseDateStart(commands[1]);
            } catch (IllegalArgumentException e) {
                sendHelp(event);
                return;
            }

            statistics = ircMessageService.getStatistics(event.getBot().getServerHostname(), event.getChannel().getName(), periodInfo.dateStart);
            if (statistics.size() == 0) {
                event.respondChannel("Statistics for "+periodInfo.period+" is empty now");
                return;
            }
            event.respondChannel("TOP 5 for "+periodInfo.period+":");

            for (int i = 0; i < 5 && i < statistics.size(); i++) {
                StatisticsDTO stats = statistics.get(i);
                event.respondChannel("" + (i + 1) + ". " + stats.getUsername() + " - " + stats.getCount());
            }

        } else if (commands.length == 2 && commands[1].equals("all")) {

            statistics = ircMessageService.getStatistics(event.getBot().getServerHostname(), event.getChannel().getName());
            if (statistics.size() == 0) {
                event.respondChannel("Statistics is empty now");
                return;
            }

            if (statistics.size() > 10) {
                event.respondPrivateMessage("TOP for all over time:");

                for (int i = 0; i < statistics.size(); i++) {
                    StatisticsDTO stats = statistics.get(i);
                    event.respondPrivateMessage("" + (i + 1) + ". " + stats.getUsername() + " - " + stats.getCount());
                }
            } else {
                event.respondChannel("TOP for all over time:");

                for (int i = 0; i < statistics.size(); i++) {
                    StatisticsDTO stats = statistics.get(i);
                    event.respondChannel("" + (i + 1) + ". " + stats.getUsername() + " - " + stats.getCount());
                }
            }

        } else if (commands.length == 3) {

            if (commands[2].equals("all")) {

                PeriodInfo periodInfo = null;
                try {
                    periodInfo = parseDateStart(commands[1]);
                } catch (IllegalArgumentException e) {
                    sendHelp(event);
                    return;
                }

                statistics = ircMessageService.getStatistics(event.getBot().getServerHostname(), event.getChannel().getName(), periodInfo.dateStart);
                if (statistics.size() == 0) {
                    event.respondChannel("Statistics for "+periodInfo.period+" is empty now");
                    return;
                }

                if (statistics.size() > 10) {
                    event.respondPrivateMessage("TOP for " + periodInfo.period + ":");

                    for (int i = 0; i < statistics.size(); i++) {
                        StatisticsDTO stats = statistics.get(i);
                        event.respondPrivateMessage("" + (i + 1) + ". " + stats.getUsername() + " - " + stats.getCount());
                    }
                } else {
                    event.respondChannel("TOP for " + periodInfo.period + ":");

                    for (int i = 0; i < statistics.size(); i++) {
                        StatisticsDTO stats = statistics.get(i);
                        event.respondChannel("" + (i + 1) + ". " + stats.getUsername() + " - " + stats.getCount());
                    }
                }

            } else {
                sendHelp(event);
            }
        }

    }

    private PeriodInfo parseDateStart(String command) {
        PeriodInfo info = new PeriodInfo();

        LocalDateTime ldt = DateTimeUtility.dateToLocalDateTime(new Date());
        if (command.equals("d") || command.equals("day")) {
            info.dateStart = DateTimeUtility.localDateTimeToDate(
                    ldt.withHour(0).withMinute(0).withSecond(0).withNano(0)
            );
            info.period = "day";
        } else if (command.equals("m") || command.equals("month")) {
            info.dateStart = DateTimeUtility.localDateTimeToDate(
                    ldt.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            );
            info.period = "month";
        } else if (command.equals("y") || command.equals("year")) {
            info.dateStart = DateTimeUtility.localDateTimeToDate(
                    ldt.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            );
            info.period = "year";
        } else {
            throw new IllegalArgumentException("");
        }
        return info;
    }

    private void sendHelp(MessageEvent event) {
        event.respond("syntax: ?stats [d(ay)|m(onth)|y(ear)|a(ll)] [all]");
    }
}
