package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bvn13.jircbot.database.entities.ChannelSettings;

import java.util.List;

/**
 * Created by bvn13 on 01.02.2018.
 */
public interface ChannelSettingsRepository extends JpaRepository<ChannelSettings, Long> {

    ChannelSettings getFirstByServerHostAndChannelName(String serverHost, String channelName);

    @Query(value = "select S from ChannelSettings S where S.joinOnStart = true and S.serverHost = :serverHost")
    List<ChannelSettings> getAllChannelsToAutoJoinByServerHost(@Param("serverHost") String serverHost);

}
