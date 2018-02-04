package ru.bvn13.jircbot.database.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bvn13.jircbot.database.entities.GrammarCorrection;

import java.util.List;

/**
 * Created by bvn13 on 03.02.2018.
 */
@Repository
public interface GrammarCorrectionRepository extends JpaRepository<GrammarCorrection, Long> {

    GrammarCorrection findFirstByWordAndCorrection(String word, String correction);

}
