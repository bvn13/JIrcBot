package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bvn13.jircbot.database.entities.DeferredMessage;

import java.util.List;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Repository
public interface DeferredMessageRepository extends JpaRepository<DeferredMessage, Long> {
    List<DeferredMessage> getDeferredMessagesByChannelNameAndRecipientAndSentOrderByCreatedAt(String channelName, String recipient, Boolean sent);
}
