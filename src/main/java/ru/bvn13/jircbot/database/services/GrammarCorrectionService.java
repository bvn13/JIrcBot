package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.bvn13.jircbot.database.entities.GrammarCorrection;
import ru.bvn13.jircbot.database.repositories.GrammarCorrectionRepository;

import javax.sql.DataSource;
import java.util.ArrayList;
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

    public List<String> getCorrectionsForMessage(String message) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<String> result = new ArrayList<>();

        try {
            jdbcTemplate.query("" +
                            "SELECT ? as message, g.word, g.correction " +
                            "FROM public.grammar_correction AS g " +
                            "WHERE ? ~ g.word", new Object[]{ message, message },
                    row -> {
                        result.add(row.getString("correction"));
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public GrammarCorrection getLastCorrection(String word) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        List<GrammarCorrection> result = jdbcTemplate.query("" +
                        "SELECT gc.id, gc.dtcreated, gc.dtupdated, gc.word, gc.correction, gc.author " +
                        "FROM " +
                        "  (SELECT gc.word, MAX(gc.dtupdated) AS dtupdated " +
                        "  FROM grammar_correction AS gc " +
                        "  WHERE word ~ ?) AS tmax " +
                        "INNER JOIN grammar_correction AS gc " +
                        "ON gc.word = tmax.word AND gc.dtupdated = tmax.dtupdated " +
                        "ORDER BY gc.dtupdated DESC",
                new Object[]{word},
                (rs, rsNum) -> new GrammarCorrection(
                        rs.getLong("id"),
                        rs.getDate("dtcreated"),
                        rs.getDate("dtupdated"),
                        rs.getString("word"),
                        rs.getString("correction"),
                        rs.getString("author")
                ));

        if (result.size() > 0) {
            return result.get(0);
        }

        return null;
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
