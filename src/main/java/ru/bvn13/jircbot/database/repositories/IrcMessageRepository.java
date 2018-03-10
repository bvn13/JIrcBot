package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bvn13.jircbot.database.entities.IrcMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by bvn13 on 10.03.2018.
 */
public interface IrcMessageRepository extends JpaRepository<IrcMessage, Long> {

    List<IrcMessage> findAllByServerHostAndChannelNameAndDtCreatedGreaterThanEqualAndDtCreatedIsLessThanEqual(String serverHost, String channelName, Date dtFrom, Date dtTo);

    //@Query("SELECT m FROM IrcMessage m WHERE m.serverHost = :serverHost AND m.channelName SIMILAR TO CONCAT('\\#+', :channelName) AND m.dtCreated >= :dtFrom AND m.dtCreated <= :dtTo")
    @Query("SELECT m FROM IrcMessage m WHERE m.serverHost = :serverHost AND (m.channelName = CONCAT('#', :channelName) OR m.channelName = CONCAT('##', :channelName)) AND m.dtCreated >= :dtFrom AND m.dtCreated <= :dtTo ORDER BY m.dtCreated")
    List<IrcMessage> findAllByServerHostAndChannelNameAndDay(@Param("serverHost") String serverHost, @Param("channelName") String channelName, @Param("dtFrom") Date dtFrom, @Param("dtTo") Date dtTo);

}
