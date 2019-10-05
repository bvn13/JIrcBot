package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.ChannelSettings;
import ru.bvn13.jircbot.database.repositories.ChannelSettingsRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by bvn13 on 01.02.2018.
 */
@Service
public class ChannelSettingsService {

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    public ChannelSettings getChannelSettings(String serverHost, String channelName) {
        ChannelSettings settings = channelSettingsRepository.getFirstByServerHostAndChannelName(serverHost, channelName);
        if (settings == null) {
            settings = new ChannelSettings();
            settings.setServerHost(serverHost);
            settings.setChannelName(channelName);
        }
        return settings;
    }

    public void createChannelSettings(String serverHost, String channelName) {
        ChannelSettings settings = channelSettingsRepository.getFirstByServerHostAndChannelName(serverHost, channelName);
        if (settings == null) {
            settings = new ChannelSettings();
            settings.setServerHost(serverHost);
            settings.setChannelName(channelName);
            channelSettingsRepository.save(settings);
        }
    }

    public void save(ChannelSettings settings) {
        channelSettingsRepository.save(settings);
    }

    public Set<String> getListeningChannels(String serverHost, List<String> defaultChannels) {
        Set<String> channels = channelSettingsRepository.getAllChannelsToAutoJoinByServerHost(serverHost).stream()
                .map(ChannelSettings::getChannelName)
                .collect(Collectors.toSet());
        if (channels.isEmpty()) {
            channels.addAll(defaultChannels);
        }
        return channels;
    }

}
