package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.ChannelSettings;
import ru.bvn13.jircbot.database.repositories.ChannelSettingsRepository;

/**
 * Created by bvn13 on 01.02.2018.
 */
@Service
public class ChannelSettingsService {

    @Autowired
    private ChannelSettingsRepository channelSettingsRepository;

    public ChannelSettings getChannelSettings(String channelName) {
        ChannelSettings settings = channelSettingsRepository.getFirstByChannelName(channelName);
        if (settings == null) {
            settings = new ChannelSettings();
            settings.setChannelName(channelName);
        }
        return settings;
    }

    public void creaateChannelSettings(String channelName) {
        ChannelSettings settings = channelSettingsRepository.getFirstByChannelName(channelName);
        if (settings == null) {
            settings = new ChannelSettings();
            settings.setChannelName(channelName);
            channelSettingsRepository.save(settings);
        }
    }

    public void save(ChannelSettings settings) {
        channelSettingsRepository.save(settings);
    }

}
