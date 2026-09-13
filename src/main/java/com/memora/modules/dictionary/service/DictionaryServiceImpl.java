package com.memora.modules.dictionary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.memora.common.exception.BadRequestException;
import com.memora.modules.dictionary.client.DictionaryProvider;
import com.memora.modules.dictionary.dto.DefinitionDto;
import com.memora.modules.dictionary.dto.DictionaryResponse;
import com.memora.modules.dictionary.dto.PartOfSpeechDto;
import com.memora.modules.dictionary.exception.DictionaryWordNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Implementation of {@link DictionaryService} coordinating validation,
 * Merriam-Webster API interaction, CDN audio URL resolution, token sanitization,
 * spelling suggestions handling, and bounded thread-safe caching.
 */
@Service
public class DictionaryServiceImpl implements DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryServiceImpl.class);

    // Permitted characters: Unicode letters, spaces, hyphens, and apostrophes (standard ' and curly ’)
    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("^[\\p{L}\\s'’\\-]+$");
    private static final int MAX_WORD_LENGTH = 64;

    // Bounded thread-safe in-memory cache (500 entries, 30-minute TTL)
    private static final int MAX_CACHE_ENTRIES = 500;
    private static final long CACHE_TTL_MILLIS = 30 * 60 * 1000L;

    private record CachedDictionaryEntry(DictionaryResponse response, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final Map<String, CachedDictionaryEntry> cache = new ConcurrentHashMap<>();
    private final DictionaryProvider dictionaryProvider;

    public DictionaryServiceImpl(DictionaryProvider dictionaryProvider) {
        this.dictionaryProvider = dictionaryProvider;
    }

    @Override
    public DictionaryResponse lookupWord(String rawWord) {
        String normalizedWord = validateAndNormalizeWord(rawWord);
        String cacheKey = "dictionary:merriam-webster:" + normalizedWord.toLowerCase(Locale.ENGLISH);

        // Check in-memory cache
        CachedDictionaryEntry cached = cache.get(cacheKey);
        if (cached != null) {
            if (!cached.isExpired()) {
                log.debug("Cache hit for dictionary word '{}'", normalizedWord);
                return cached.response();
            } else {
                cache.remove(cacheKey);
            }
        }

        JsonNode rootNode = dictionaryProvider.fetchWordEntries(normalizedWord);
        DictionaryResponse response = parseAndConsolidateEntries(rootNode, normalizedWord);

        // Cache valid entry with bounds management
        if (cache.size() >= MAX_CACHE_ENTRIES) {
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            if (cache.size() >= MAX_CACHE_ENTRIES) {
                // Evict roughly 20% oldest entries
                cache.keySet().stream().limit(MAX_CACHE_ENTRIES / 5).forEach(cache::remove);
            }
        }
        cache.put(cacheKey, new CachedDictionaryEntry(response, System.currentTimeMillis() + CACHE_TTL_MILLIS));

        return response;
    }

    /**
     * Validates and trims search query input.
     */
    public String validateAndNormalizeWord(String rawWord) {
        if (rawWord == null || rawWord.trim().isEmpty()) {
            throw new BadRequestException("Search word cannot be empty");
        }

        String trimmed = rawWord.trim();

        if (trimmed.length() > MAX_WORD_LENGTH) {
            throw new BadRequestException("Search word cannot exceed " + MAX_WORD_LENGTH + " characters");
        }

        if (!VALID_WORD_PATTERN.matcher(trimmed).matches()) {
            throw new BadRequestException("Invalid search word. Only letters, hyphens, and apostrophes are allowed.");
        }

        return trimmed;
    }

    /**
     * Resolves official Merriam-Webster audio references into complete CDN MP3 URLs according to
     * Merriam-Webster's documented subdirectory rules.
     *
     * Rules:
     * - if audio begins with "bix", subdirectory is "bix"
     * - if audio begins with "gg", subdirectory is "gg"
     * - if audio begins with a number or punctuation, subdirectory is "number"
     * - otherwise, subdirectory is the first character of the audio filename
     */
    public String resolveMerriamWebsterAudioUrl(String audio) {
        if (audio == null || audio.isBlank()) {
            return null;
        }

        String trimmed = audio.trim();
        String subdirectory;

        if (trimmed.startsWith("bix")) {
            subdirectory = "bix";
        } else if (trimmed.startsWith("gg")) {
            subdirectory = "gg";
        } else if (Character.isDigit(trimmed.charAt(0)) || !Character.isLetter(trimmed.charAt(0))) {
            subdirectory = "number";
        } else {
            subdirectory = String.valueOf(Character.toLowerCase(trimmed.charAt(0)));
        }

        return "https://media.merriam-webster.com/audio/prons/en/us/mp3/" + subdirectory + "/" + trimmed + ".mp3";
    }

    /**
     * Strips and normalizes Merriam-Webster formatting tokens (e.g. {bc}, {it}, {a_link|...}, {sx|...}).
     */
    public String cleanMwTokens(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String cleaned = text;

        // Replace bold colon with colon-space
        cleaned = cleaned.replaceAll("\\{bc\\}", ": ");

        // Replace link and synonymous cross-reference tokens with their target words
        cleaned = cleaned.replaceAll("\\{(?:a_link|d_link|sx|dxt|i_link|mat)\\|([^}|]+)(?:\\|[^}]*)?\\}", "$1");

        // Remove markup tags: {it}, {/it}, {b}, {/b}, {wi}, {/wi}, {phrase}, {/phrase}, {sc}, {/sc}, etc.
        cleaned = cleaned.replaceAll("\\{/?[a-zA-Z0-9_]+\\}", "");

        // Strip any residual formatting tokens
        cleaned = cleaned.replaceAll("\\{[^}]*\\}", "");

        // Collapse whitespace
        cleaned = cleaned.replaceAll("\\s{2,}", " ");

        // Strip leading colon/space
        cleaned = cleaned.replaceAll("^\\s*:\\s*", "");

        return cleaned.trim();
    }

    /**
     * Parses the Merriam-Webster Collegiate JSON response array and returns a clean, consolidated
     * {@link DictionaryResponse}. Handles spelling suggestions, multiple entries, and missing fields gracefully.
     */
    public DictionaryResponse parseAndConsolidateEntries(JsonNode rootNode, String fallbackWord) {
        if (rootNode == null || !rootNode.isArray() || rootNode.isEmpty()) {
            throw new DictionaryWordNotFoundException(fallbackWord);
        }

        // Case 1: Merriam-Webster returned an array of spelling suggestions (Strings)
        if (rootNode.get(0).isTextual()) {
            List<String> suggestions = new ArrayList<>();
            for (JsonNode sNode : rootNode) {
                if (sNode.isTextual()) {
                    String s = sNode.asText("").trim();
                    if (!s.isEmpty()) {
                        suggestions.add(s);
                    }
                }
            }

            if (suggestions.isEmpty()) {
                throw new DictionaryWordNotFoundException(fallbackWord);
            }

            return new DictionaryResponse(
                    fallbackWord,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    null,
                    suggestions
            );
        }

        // Case 2: Array of full entry objects
        String resolvedWord = null;
        String headword = null;
        String pronunciation = null;
        String audioUrl = null;
        String etymology = null;

        Map<String, List<DefinitionDto>> posMap = new LinkedHashMap<>();
        Set<String> shortDefsSet = new LinkedHashSet<>();

        for (JsonNode entryNode : rootNode) {
            if (!entryNode.isObject()) {
                continue;
            }

            // Word from meta.id (e.g. "happy:1" -> "happy")
            if (resolvedWord == null && entryNode.hasNonNull("meta") && entryNode.get("meta").hasNonNull("id")) {
                String metaId = entryNode.get("meta").get("id").asText("").trim();
                int colonIdx = metaId.indexOf(':');
                resolvedWord = colonIdx > 0 ? metaId.substring(0, colonIdx) : metaId;
            }

            // Headword from hwi.hw (e.g. "hap*py" -> "hap·py")
            if (headword == null && entryNode.hasNonNull("hwi") && entryNode.get("hwi").hasNonNull("hw")) {
                String rawHw = entryNode.get("hwi").get("hw").asText("").trim();
                if (!rawHw.isEmpty()) {
                    headword = rawHw.replace('*', '·');
                }
            }

            // Pronunciation & Audio from hwi.prs
            if (entryNode.hasNonNull("hwi") && entryNode.get("hwi").has("prs") && entryNode.get("hwi").get("prs").isArray()) {
                for (JsonNode prsNode : entryNode.get("hwi").get("prs")) {
                    if (pronunciation == null) {
                        if (prsNode.hasNonNull("mw")) {
                            String mw = prsNode.get("mw").asText("").trim();
                            if (!mw.isEmpty()) {
                                pronunciation = mw;
                            }
                        } else if (prsNode.hasNonNull("ipa")) {
                            String ipa = prsNode.get("ipa").asText("").trim();
                            if (!ipa.isEmpty()) {
                                pronunciation = ipa;
                            }
                        }
                    }

                    if (audioUrl == null && prsNode.hasNonNull("sound") && prsNode.get("sound").hasNonNull("audio")) {
                        String rawAudio = prsNode.get("sound").get("audio").asText("").trim();
                        String resolved = resolveMerriamWebsterAudioUrl(rawAudio);
                        if (resolved != null) {
                            audioUrl = resolved;
                        }
                    }
                }
            }

            // Etymology from et
            if (etymology == null && entryNode.hasNonNull("et") && entryNode.get("et").isArray()) {
                StringBuilder etBuilder = new StringBuilder();
                for (JsonNode etItem : entryNode.get("et")) {
                    if (etItem.isArray() && etItem.size() >= 2 && "text".equals(etItem.get(0).asText())) {
                        String cleanedEt = cleanMwTokens(etItem.get(1).asText());
                        if (!cleanedEt.isEmpty()) {
                            if (!etBuilder.isEmpty()) {
                                etBuilder.append(" ");
                            }
                            etBuilder.append(cleanedEt);
                        }
                    }
                }
                if (!etBuilder.isEmpty()) {
                    etymology = etBuilder.toString();
                }
            }

            // Part of speech from fl
            String pos = entryNode.hasNonNull("fl") ? entryNode.get("fl").asText("").trim() : "general";
            if (pos.isEmpty()) {
                pos = "general";
            }

            List<DefinitionDto> posDefinitions = posMap.computeIfAbsent(pos, k -> new ArrayList<>());

            // Parse detailed definitions and examples from def -> sseq -> sense -> dt
            boolean extractedDetailedDef = false;
            if (entryNode.hasNonNull("def") && entryNode.get("def").isArray()) {
                for (JsonNode defNode : entryNode.get("def")) {
                    if (defNode.hasNonNull("sseq") && defNode.get("sseq").isArray()) {
                        for (JsonNode sseqRow : defNode.get("sseq")) {
                            if (!sseqRow.isArray()) continue;
                            for (JsonNode item : sseqRow) {
                                if (item.isArray() && item.size() >= 2) {
                                    String itemType = item.get(0).asText();
                                    if ("sense".equals(itemType) || "bs".equals(itemType)) {
                                        JsonNode senseNode = item.get(1);
                                        if (senseNode.hasNonNull("dt") && senseNode.get("dt").isArray()) {
                                            String defText = null;
                                            List<String> examples = new ArrayList<>();

                                            for (JsonNode dtItem : senseNode.get("dt")) {
                                                if (dtItem.isArray() && dtItem.size() >= 2) {
                                                    String dtType = dtItem.get(0).asText();
                                                    if ("text".equals(dtType) && defText == null) {
                                                        String cleaned = cleanMwTokens(dtItem.get(1).asText());
                                                        if (!cleaned.isEmpty()) {
                                                            defText = cleaned;
                                                        }
                                                    } else if ("vis".equals(dtType) && dtItem.get(1).isArray()) {
                                                        for (JsonNode visItem : dtItem.get(1)) {
                                                            if (visItem.hasNonNull("t")) {
                                                                String cleanedEx = cleanMwTokens(visItem.get("t").asText());
                                                                if (!cleanedEx.isEmpty()) {
                                                                    examples.add(cleanedEx);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (defText != null && !defText.isEmpty()) {
                                                posDefinitions.add(new DefinitionDto(defText, examples));
                                                extractedDetailedDef = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Short definitions from shortdef
            if (entryNode.hasNonNull("shortdef") && entryNode.get("shortdef").isArray()) {
                for (JsonNode sNode : entryNode.get("shortdef")) {
                    String sd = sNode.asText("").trim();
                    if (!sd.isEmpty()) {
                        shortDefsSet.add(sd);
                        // If detailed definitions were empty, use shortdef as fallback definition
                        if (!extractedDetailedDef) {
                            posDefinitions.add(new DefinitionDto(sd, List.of()));
                        }
                    }
                }
            }
        }

        if (resolvedWord == null || resolvedWord.isEmpty()) {
            resolvedWord = fallbackWord;
        }

        if (headword == null || headword.isEmpty()) {
            headword = resolvedWord;
        }

        // Convert grouped parts of speech map to PartOfSpeechDto list
        List<PartOfSpeechDto> partsOfSpeech = new ArrayList<>();
        for (Map.Entry<String, List<DefinitionDto>> entry : posMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                partsOfSpeech.add(new PartOfSpeechDto(entry.getKey(), entry.getValue()));
            }
        }

        return new DictionaryResponse(
                resolvedWord,
                headword,
                pronunciation,
                audioUrl,
                partsOfSpeech,
                new ArrayList<>(shortDefsSet),
                etymology,
                List.of()
        );
    }

    public void clearCache() {
        cache.clear();
    }
}
