package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.IrcMessage;
import ru.bvn13.jircbot.database.repositories.IrcMessageRepository;

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

}
