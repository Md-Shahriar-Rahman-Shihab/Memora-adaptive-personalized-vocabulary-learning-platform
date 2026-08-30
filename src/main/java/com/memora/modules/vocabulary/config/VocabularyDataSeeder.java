package com.memora.modules.vocabulary.config;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Data seeder populating curated vocabulary dataset on initial system bootstrap.
 * Idempotent: Executes only when the vocabulary catalog is empty.
 */
@Component
public class VocabularyDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VocabularyDataSeeder.class);

    private final VocabularyWordRepository vocabularyWordRepository;

    public VocabularyDataSeeder(VocabularyWordRepository vocabularyWordRepository) {
        this.vocabularyWordRepository = vocabularyWordRepository;
    }

    @Override
    public void run(String... args) {
        if (vocabularyWordRepository.count() > 0) {
            log.info("Vocabulary catalog already seeded ({} words found). Skipping initialization.", vocabularyWordRepository.count());
            return;
        }

        log.info("Seeding initial curated vocabulary dataset across CEFR levels A1-B2...");

        List<VocabularyWord> initialWords = List.of(
                // A1 Level (Beginner / Foundational)
                new VocabularyWord("happy", "feeling or showing pleasure or contentment",
                        "Feeling or showing pleasure or contentment.", "/ˈhæp.i/",
                        "She was happy to see her childhood friend again.",
                        DifficultyLevel.A1, WordCategory.DAILY_LIFE),
                new VocabularyWord("small", "of a size that is less than normal or usual",
                        "Of a size that is less than normal or usual.", "/smɔːl/",
                        "They live in a small but cozy apartment in the city.",
                        DifficultyLevel.A1, WordCategory.GENERAL),
                new VocabularyWord("water", "a clear, colorless, odorless liquid essential for plant and animal life",
                        "A clear liquid essential for all living organisms.", "/ˈwɔː.tər/",
                        "Make sure to drink plenty of fresh water every day.",
                        DifficultyLevel.A1, WordCategory.DAILY_LIFE),
                new VocabularyWord("friend", "a person whom one knows and with whom one has a bond of mutual affection",
                        "A person who has a bond of mutual affection with another.", "/frend/",
                        "A true friend is always there during difficult times.",
                        DifficultyLevel.A1, WordCategory.DAILY_LIFE),
                new VocabularyWord("book", "a written or printed work consisting of pages glued or sewn together along one side",
                        "A set of printed pages bound together inside a cover.", "/bʊk/",
                        "He spends his evenings reading a fascinating history book.",
                        DifficultyLevel.A1, WordCategory.ACADEMIC),
                new VocabularyWord("school", "an institution for educating children",
                        "An educational institution where instruction is given.", "/skuːl/",
                        "The new elementary school opens early every morning.",
                        DifficultyLevel.A1, WordCategory.ACADEMIC),
                new VocabularyWord("travel", "make a journey, typically of some length",
                        "To go from one place to another over a distance.", "/ˈtræv.əl/",
                        "They plan to travel across Europe next summer.",
                        DifficultyLevel.A1, WordCategory.TRAVEL),
                new VocabularyWord("family", "a group of one or more parents and their children living together as a unit",
                        "A group of people related by blood or marriage.", "/ˈfæm.əl.i/",
                        "Spending quality time with family brings lasting joy.",
                        DifficultyLevel.A1, WordCategory.DAILY_LIFE),

                // A2 Level (Elementary / Everyday communication)
                new VocabularyWord("careful", "making sure of avoiding potential danger, mishap, or harm",
                        "Prudent and taking care to avoid risks or mistakes.", "/ˈkeə.fəl/",
                        "Be careful when walking on the icy pavement.",
                        DifficultyLevel.A2, WordCategory.GENERAL),
                new VocabularyWord("journey", "an act of traveling from one place to another",
                        "The act of traveling from a starting point to a destination.", "/ˈdʒɜː.ni/",
                        "Their journey through the mountain pass took several hours.",
                        DifficultyLevel.A2, WordCategory.TRAVEL),
                new VocabularyWord("improve", "make or become better",
                        "To develop or enhance the quality or condition of something.", "/ɪmˈpruːv/",
                        "Daily practice will greatly improve your vocabulary retention.",
                        DifficultyLevel.A2, WordCategory.ACADEMIC),
                new VocabularyWord("popular", "liked, admired, or enjoyed by many people or by a particular person or group",
                        "Widely liked, favored, or accepted by the public.", "/ˈpɒp.jə.lər/",
                        "That podcast is very popular among university students.",
                        DifficultyLevel.A2, WordCategory.GENERAL),
                new VocabularyWord("weather", "the state of the atmosphere at a place and time",
                        "Atmospheric conditions regarding temperature, wind, and rain.", "/ˈweð.ər/",
                        "The sunny weather made our outdoor picnic delightful.",
                        DifficultyLevel.A2, WordCategory.DAILY_LIFE),
                new VocabularyWord("advice", "guidance or recommendations offered with regard to prudent future action",
                        "An opinion or recommendation given to guide someone.", "/ədˈvaɪs/",
                        "She gave me valuable advice on preparing for the interview.",
                        DifficultyLevel.A2, WordCategory.BUSINESS),
                new VocabularyWord("decide", "come to a resolution in the mind as a result of consideration",
                        "To make a choice or come to a conclusion after thinking.", "/dɪˈsaɪd/",
                        "He must decide whether to accept the overseas scholarship.",
                        DifficultyLevel.A2, WordCategory.ACADEMIC),
                new VocabularyWord("explore", "travel in or through an unfamiliar area in order to learn about it",
                        "To inquire into or travel through an unfamiliar region to discover it.", "/ɪkˈsplɔːr/",
                        "We hired a local guide to explore the ancient ruins.",
                        DifficultyLevel.A2, WordCategory.TRAVEL),

                // B1 Level (Intermediate / Independent User)
                new VocabularyWord("abundant", "existing or available in large quantities; plentiful",
                        "Present in great quantity or ample supply.", "/əˈbʌn.dənt/",
                        "The region is renowned for its abundant natural resources.",
                        DifficultyLevel.B1, WordCategory.SCIENCE),
                new VocabularyWord("reluctant", "unwilling and hesitant; disinclined",
                        "Feeling hesitation or unwillingness to act.", "/rɪˈlʌk.tənt/",
                        "He was reluctant to commit before reviewing the legal contract.",
                        DifficultyLevel.B1, WordCategory.BUSINESS),
                new VocabularyWord("persistent", "continuing firmly in a course of action in spite of difficulty or opposition",
                        "Refusing to give up despite hurdles or obstacles.", "/pəˈsɪs.tənt/",
                        "Her persistent effort led to a major scientific breakthrough.",
                        DifficultyLevel.B1, WordCategory.ACADEMIC),
                new VocabularyWord("accurate", "correct in all details; exact",
                        "Conforming exactly to truth or standard facts.", "/ˈæk.jə.rət/",
                        "The financial report provided an accurate summary of quarterly earnings.",
                        DifficultyLevel.B1, WordCategory.BUSINESS),
                new VocabularyWord("essential", "absolutely necessary; extremely important",
                        "Indispensable and fundamental to the nature of something.", "/ɪˈsen.ʃəl/",
                        "Critical thinking is an essential skill for modern software engineers.",
                        DifficultyLevel.B1, WordCategory.ACADEMIC),
                new VocabularyWord("convenient", "fitting in well with a person's needs, activities, and plans",
                        "Suited to personal comfort or ease of performance.", "/kənˈviː.ni.ənt/",
                        "The subway station is convenient for daily commuters.",
                        DifficultyLevel.B1, WordCategory.DAILY_LIFE),
                new VocabularyWord("diverse", "showing a great deal of variety; very different",
                        "Differing from one another; composed of distinct qualities.", "/daɪˈvɜːs/",
                        "The international conference gathered scholars from diverse backgrounds.",
                        DifficultyLevel.B1, WordCategory.ACADEMIC),
                new VocabularyWord("efficient", "achieving maximum productivity with minimum wasted effort or expense",
                        "Performing in the best possible manner with least waste of time and effort.", "/ɪˈfɪʃ.ənt/",
                        "The newly deployed algorithm provides an efficient sorting mechanism.",
                        DifficultyLevel.B1, WordCategory.TECHNOLOGY),

                // B2 Level (Upper Intermediate / Complex & Abstract)
                new VocabularyWord("inevitable", "certain to happen; unavoidable",
                        "Incapable of being avoided, prevented, or evaded.", "/ɪnˈev.ɪ.tə.bəl/",
                        "Technological disruption is an inevitable part of modern industry evolution.",
                        DifficultyLevel.B2, WordCategory.TECHNOLOGY),
                new VocabularyWord("substantial", "of considerable importance, size, or worth",
                        "Ample, considerable, and firmly established.", "/səbˈstæn.ʃəl/",
                        "The company invested a substantial amount of capital in green energy.",
                        DifficultyLevel.B2, WordCategory.BUSINESS),
                new VocabularyWord("ambiguous", "open to more than one interpretation; having a double meaning",
                        "Unclear or capable of being understood in multiple ways.", "/æmˈbɪɡ.ju.əs/",
                        "The contract's wording was ambiguous and caused disagreement.",
                        DifficultyLevel.B2, WordCategory.ACADEMIC),
                new VocabularyWord("contemporary", "living or occurring at the same time; modern",
                        "Belonging to or occurring in the present era.", "/kənˈtem.pər.ər.i/",
                        "The art gallery features striking contemporary sculptures.",
                        DifficultyLevel.B2, WordCategory.ACADEMIC),
                new VocabularyWord("comprehensive", "complete; including all or nearly all elements or aspects of something",
                        "Broad in scope and covering all essential facets.", "/ˌkɒm.prɪˈhen.sɪv/",
                        "The professor provided a comprehensive syllabus for the laboratory course.",
                        DifficultyLevel.B2, WordCategory.ACADEMIC),
                new VocabularyWord("preliminary", "denoting an action or event preceding or done in preparation for something fuller or more important",
                        "Preceding and leading up to the main event or work.", "/prɪˈlɪm.ɪ.nər.i/",
                        "Preliminary findings indicate strong positive correlation between sleep and retention.",
                        DifficultyLevel.B2, WordCategory.SCIENCE),
                new VocabularyWord("coherent", "logical and consistent in argument, theory, or policy",
                        "Logically structured, lucid, and easy to follow.", "/kəʊˈhɪə.rənt/",
                        "She presented a coherent analysis of the economic dataset.",
                        DifficultyLevel.B2, WordCategory.ACADEMIC),
                new VocabularyWord("versatile", "able to adapt or be adapted to many different functions or activities",
                        "Capable of turning with ease from one task or subject to another.", "/ˈvɜː.sə.taɪl/",
                        "Java is a versatile programming language used across cloud and enterprise platforms.",
                        DifficultyLevel.B2, WordCategory.TECHNOLOGY)
        );

        vocabularyWordRepository.saveAll(initialWords);
        log.info("Successfully seeded {} vocabulary words across CEFR levels A1, A2, B1, B2.", initialWords.size());
    }
}
