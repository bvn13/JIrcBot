package ru.bvn13.jircbot.database.services;

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

    @Autowired
    private IrcMessageRepository ircMessageRepository;

    public void save(IrcMessage message) {
        ircMessageRepository.save(message);
    }

    public List<IrcMessage> getMessagesOfDay(String serverHost, String channelName, Date date) {
        LocalDateTime localDateTime = DateTimeUtility.dateToLocalDateTime(date);

        LocalDateTime dtFrom = localDateTime.with(LocalTime.MIN);
        LocalDateTime dtTo = localDateTime.with(LocalTime.MAX);

        return ircMessageRepository.findAllByServerHostAndChannelNameAndDay(
                serverHost,
                channelName,
                DateTimeUtility.localDateTimeToDate(dtFrom),
                DateTimeUtility.localDateTimeToDate(dtTo)
        );

        /*return ircMessageRepository.findAllByServerHostAndChannelNameAndDtCreatedGreaterThanEqualAndDtCreatedIsLessThanEqual(
                serverHost,
                channelName,
                DateTimeUtility.localDateTimeToDate(dtFrom),
                DateTimeUtility.localDateTimeToDate(dtTo)
        );*/
    }

}
