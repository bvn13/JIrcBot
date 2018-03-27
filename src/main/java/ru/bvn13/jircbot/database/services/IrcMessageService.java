package ru.bvn13.jircbot.database.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.IrcMessage;
import ru.bvn13.jircbot.database.repositories.IrcMessageRepository;
import ru.bvn13.jircbot.utilities.DateTimeUtility;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by bvn13 on 10.03.2018.
 */
@Service
public class IrcMessageService {

    private static final Logger logger = LoggerFactory.getLogger(IrcMessageService.class);


    @Autowired
    private IrcMessageRepository ircMessageRepository;

    public void save(IrcMessage message) {
        try {
            ircMessageRepository.save(message);
        } catch (Exception e) {
            logger.error("Could not save message! "+message, e);
        }
    }

    public List<IrcMessage> getMessagesOfDay(String serverHost, String channelName, Date date) {
        LocalDateTime localDateTime = DateTimeUtility.dateToLocalDateTime(date);

        LocalDateTime dtFrom = localDateTime.with(LocalTime.MIN);
        LocalDateTime dtTo = localDateTime.with(LocalTime.MAX);

        String realChannelName = channelName;
        String prefix = "";
        while (realChannelName.substring(0, 1).equals("_")) {
            prefix += "#";
            realChannelName = realChannelName.substring(1);
        }
        realChannelName = prefix + realChannelName;

        return ircMessageRepository.findAllByServerHostAndRealChannelNameAndDay(
                serverHost,
                realChannelName,
                DateTimeUtility.localDateTimeToDate(dtFrom),
                DateTimeUtility.localDateTimeToDate(dtTo)
        );

    }

}
