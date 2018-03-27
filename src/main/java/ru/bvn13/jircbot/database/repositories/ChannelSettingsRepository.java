package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bvn13.jircbot.database.entities.ChannelSettings;

/**
 * Created by bvn13 on 01.02.2018.
 */
public interface ChannelSettingsRepository extends JpaRepository<ChannelSettings, Long> {
    ChannelSettings getFirstByServerHostAndChannelName(String serverHost, String channelName);
}
