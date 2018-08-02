package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bvn13.jircbot.database.entities.DeferredMessage;

import java.util.List;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Repository
public interface DeferredMessageRepository extends JpaRepository<DeferredMessage, Long> {
    List<DeferredMessage> getDeferredMessagesByChannelNameAndRecipientAndSentOrderByDtCreated(String channelName, String recipient, Boolean sent);
    @Query(nativeQuery=true,
            value = "select d.* from DeferredMessage as d where d.channelName = :channelName and :ident ~ d.recipientIdent and d.sent = :sent")
    List<DeferredMessage> getDeferredMessagesByChannelNameAndRecipientIdentAndSentOrderByDtCreated(@Param("channelName") String channelName, @Param("ident") String recipientIdent, @Param("sent") Boolean sent);
}
