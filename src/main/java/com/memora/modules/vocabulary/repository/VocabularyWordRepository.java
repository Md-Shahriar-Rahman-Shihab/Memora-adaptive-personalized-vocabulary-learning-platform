package com.memora.modules.vocabulary.repository;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link VocabularyWord} operations.
 */
@Repository
public interface VocabularyWordRepository extends JpaRepository<VocabularyWord, Long> {

    Optional<VocabularyWord> findByWordIgnoreCase(String word);

    boolean existsByWordIgnoreCase(String word);

    List<VocabularyWord> findByDifficultyLevel(DifficultyLevel difficultyLevel);

    List<VocabularyWord> findByCategory(WordCategory category);

    @Query("SELECT v FROM VocabularyWord v WHERE LOWER(v.word) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<VocabularyWord> searchByWordOrMeaning(@Param("query") String query);

    @Query("SELECT v FROM VocabularyWord v ORDER BY FUNCTION('RAND')")
    List<VocabularyWord> findRandomWordsH2(Pageable pageable);

    @Query("SELECT v FROM VocabularyWord v ORDER BY RANDOM()")
    List<VocabularyWord> findRandomWordsPostgres(Pageable pageable);
}
