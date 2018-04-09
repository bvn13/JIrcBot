package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.DeferredMessage;
import ru.bvn13.jircbot.database.repositories.DeferredMessageRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Service
public class DeferredMessageService {

    @Autowired
    private DeferredMessageRepository deferredMessageRepository;

    public List<DeferredMessage> getDeferredMessagesForUser(String channelName, String user) {
        return deferredMessageRepository.getDeferredMessagesByChannelNameAndRecipientAndSentOrderByDtCreated(channelName, user, false);
    }

    public void saveDeferredMessage(String channelName, String sender, String recipient, String message) {
        DeferredMessage msg = new DeferredMessage();
        msg.setChannelName(channelName);
        msg.setSender(sender);
        msg.setRecipient(recipient);
        msg.setMessage(message);
        msg.setSent(false);
        deferredMessageRepository.save(msg);
    }

    public void markMessageWasSent(DeferredMessage msg) {
        msg.setSent(true);
        deferredMessageRepository.save(msg);
    }

    public void forgetAllMessages(String channelName, String recipient) {
        List<DeferredMessage> messages = deferredMessageRepository.getDeferredMessagesByChannelNameAndRecipientAndSentOrderByDtCreated(channelName, recipient, false);
        messages.forEach(msg -> {
            msg.setSent(false);
            deferredMessageRepository.save(msg);
        });
    }

}
