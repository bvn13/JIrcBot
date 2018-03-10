package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.GrammarCorrection;
import ru.bvn13.jircbot.database.repositories.GrammarCorrectionRepository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by bvn13 on 03.02.2018.
 */
@Service
public class GrammarCorrectionService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private GrammarCorrectionRepository grammarCorrectionRepository;

    public HashMap<String, String[]> getCorrectionsForMessage(String message) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        HashMap<String, String[]> result = new HashMap<>();

        try {
            jdbcTemplate.query("" +
                            "SELECT ? as message, g.word, g.correction " +
                            "FROM public.grammar_correction AS g " +
                            "WHERE " +
                            "NOT g.correction = '~' " +
                            "AND " +
                            "CASE WHEN g.regexp THEN " +
                            "? ~ concat('(\\A|\\s)(', g.word, ')(\\Z|\\s)') " +
                            "ELSE " +
                            "? ~ concat('(', g.word , ')') " +
                            "END ", new Object[]{ message, message, message },
                    row -> {
                        result.put(row.getString("correction"),
                                new String[] {
                                        row.getString("message"),
                                        row.getString("word"),
                                        row.getString("correction")
                                }
                                );
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void saveGrammarCorrection(String word, String correction, String author) {
        GrammarCorrection gc = grammarCorrectionRepository.findFirstByWordAndCorrection(word, correction);
        if (gc == null) {
            gc = new GrammarCorrection();
            gc.setWord(word);
            gc.setCorrection(correction);
        }
        gc.setAuthor(author);
        grammarCorrectionRepository.save(gc);
    }

    public Boolean removeCorrection(String word, String correction) {
        GrammarCorrection gc = grammarCorrectionRepository.findFirstByWordAndCorrection(word, correction);
        if (gc != null) {
            grammarCorrectionRepository.delete(gc);
            return true;
        }
        return false;
    }

    public Boolean removeAllCorrectionsByWord(String word) {
        List<GrammarCorrection> gcList = grammarCorrectionRepository.findAllByWord(word);
        gcList.forEach(gc -> {
            grammarCorrectionRepository.delete(gc);
        });
        return gcList.size() > 0;
    }

    public List<GrammarCorrection> getAllCorrections() {
        List<GrammarCorrection> list = grammarCorrectionRepository.findAll(new Sort(Sort.Direction.DESC, "id"));
        return list;
    }

}
