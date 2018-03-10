package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bvn13.jircbot.database.entities.IrcMessage;

/**
 * Created by bvn13 on 10.03.2018.
 */
public interface IrcMessageRepository extends JpaRepository<IrcMessage, Long> {

}
