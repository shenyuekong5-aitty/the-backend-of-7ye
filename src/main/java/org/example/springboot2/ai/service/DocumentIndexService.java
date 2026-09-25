package org.example.springboot2.ai.service;

import jakarta.annotation.PostConstruct;
import org.example.springboot2.ai.model.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class DocumentIndexService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIndexService.class);

    /** 意图词 -> 资源类型兜底映射：用于“推荐一首歌/游戏/书/番剧”这类不含资料关键词的查询 */
    private static final Map<String, List<String>> INTENT_TYPE_KEYWORDS = buildIntentKeywords();

    private static Map<String, List<String>> buildIntentKeywords() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("music", List.of("歌", "音乐", "歌曲", "music", "song"));
        map.put("games", List.of("游戏", "game", "games"));
        map.put("books", List.of("书", "书籍", "小说", "书单", "阅读", "book", "books", "read", "reading"));
        map.put("anime", List.of("番剧", "动漫", "动画", "anime"));
        map.put("quotes", List.of("名言", "语录", "句子", "说过", "quote", "quotes"));
        map.put("creed", List.of("信条", "信念", "座右铭", "motto", "creed"));
        map.put("nicknames", List.of("昵称", "称号", "外号", "别称", "nickname"));
        map.put("cognition", List.of("认知", "看法", "命题", "观念", "思考", "cognition"));
        map.put("propositions", List.of("立场", "观点", "主张", "proposition"));
        map.put("comments", List.of("评价", "评论", "评价语", "comment", "comments"));
        map.put("learning", List.of("学习", "在学", "课程", "教程", "技能计划", "learning", "study"));
        return Collections.unmodifiableMap(map);
    }

    private final List<Document> allDocuments = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:ai-profile/resources/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;
                String type = filename.replace(".md", "");
                parseResourceFile(resource.getInputStream(), type, filename);
            }
            log.info("AI DocumentIndexService loaded {} documents from {} files", allDocuments.size(), resources.length);
        } catch (Exception e) {
            log.error("Failed to load AI profile documents", e);
        }
    }

    private void parseResourceFile(InputStream inputStream, String type, String sourceFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String fullText = reader.lines().collect(Collectors.joining("\n"));
            String[] entries = fullText.split("(?m)^## ");
            for (int i = 1; i < entries.length; i++) {
                String entryText = entries[i].trim();
                if (entryText.isEmpty()) continue;
                Document doc = parseEntry(entryText, type, sourceFile);
                if (doc != null) {
                    allDocuments.add(doc);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse resource file: {}", sourceFile, e);
        }
    }

    private Document parseEntry(String entryText, String type, String sourceFile) {
        Document doc = new Document();
        doc.setType(type);
        doc.setSourceFile(sourceFile);
        doc.setContent(entryText);

        String[] lines = entryText.split("\n");
        String title = lines[0].trim();
        doc.setTitle(title);

        Map<String, String> fields = new HashMap<>();
        for (String line : lines) {
            if (line.startsWith("- ")) {
                int colonIndex = line.indexOf("：");
                if (colonIndex == -1) colonIndex = line.indexOf(": ");
                if (colonIndex == -1) continue;
                String key = line.substring(2, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                fields.put(key, value);
                if ("ID".equals(key)) doc.setId(value);
                if ("权限".equals(key)) doc.setPermission(value);
            }
        }

        if (doc.getPermission() == null || doc.getPermission().isEmpty()) {
            doc.setPermission("Owner");
        }
        doc.setFields(fields);

        return doc;
    }

    public List<Document> getDocumentsByRole(String role) {
        return allDocuments.stream()
                .filter(d -> d.isAccessibleBy(role))
                .collect(Collectors.toList());
    }

    public List<Document> search(List<Document> candidates, String query, int topK) {
        if (candidates == null || candidates.isEmpty() || query == null || query.isBlank()) {
            return List.of();
        }
        String lowerQuery = query.toLowerCase();

        List<ScoredDocument> scored = new ArrayList<>();
        for (Document doc : candidates) {
            double score = computeRelevance(doc, lowerQuery);
            if (score > 0) {
                scored.add(new ScoredDocument(doc, score));
            }
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<Document> result = new ArrayList<>();
        Set<String> pickedIds = new HashSet<>();

        // 每种命中的资源类型至少返回一条（如“推荐一首歌和一本小说”同时命中 music/books）
        Map<String, ScoredDocument> firstByType = new LinkedHashMap<>();
        for (ScoredDocument sd : scored) {
            firstByType.putIfAbsent(sd.document.getType(), sd);
        }
        for (ScoredDocument sd : firstByType.values()) {
            if (result.size() >= topK) break;
            result.add(sd.document);
            pickedIds.add(sd.document.getId());
        }
        for (ScoredDocument sd : scored) {
            if (result.size() >= topK) break;
            if (pickedIds.contains(sd.document.getId())) continue;
            result.add(sd.document);
            pickedIds.add(sd.document.getId());
        }
        return result;
    }

    /**
     * 按指定资源类型取尚未展示过的条目，用于“再推荐一首”这类无关键词续问。
     * 全部条目都已展示过时会从头重取，保证总能返回内容。
     */
    public List<Document> nextForTypes(List<Document> candidates, Collection<String> types,
                                       Collection<String> excludeIds, int topK) {
        if (candidates == null || candidates.isEmpty() || types == null || types.isEmpty()) {
            return List.of();
        }
        Set<String> excluded = excludeIds == null ? Set.of() : new HashSet<>(excludeIds);
        List<Document> fresh = candidates.stream()
                .filter(d -> types.contains(d.getType()))
                .filter(d -> !excluded.contains(d.getId()))
                .collect(Collectors.toList());
        if (fresh.isEmpty()) {
            fresh = candidates.stream()
                    .filter(d -> types.contains(d.getType()))
                    .collect(Collectors.toList());
        }
        return fresh.stream().limit(topK).collect(Collectors.toList());
    }

    private double computeRelevance(Document doc, String query) {
        String content = doc.getContent().toLowerCase();
        String title = doc.getTitle().toLowerCase();
        double score = 0;

        String[] terms = query.split("\\s+");
        for (String term : terms) {
            if (term.length() < 2) continue;
            if (title.contains(term)) {
                score += 10.0 * (term.length() / (double) query.length());
            }
            int count = countOccurrences(content, term);
            if (count > 0) {
                score += count * 2.0 * (term.length() / (double) query.length());
            }
        }

        score += matchIntentScore(doc.getType(), query);
        return score;
    }

    /**
     * 意图词兜底分：查询命中某资源类型的意图词（如“歌/音乐/游戏/书/番剧”）时，
     * 给该类条目一个低基线分，确保“推荐一首歌”这类查询也能召回对应资源。
     * 候选集已按角色权限过滤，因此该兜底不会越权返回更高权限的资料。
     */
    private double matchIntentScore(String type, String query) {
        List<String> keywords = INTENT_TYPE_KEYWORDS.get(type);
        if (keywords == null) return 0;
        for (String keyword : keywords) {
            if (query.contains(keyword)) {
                return 5.0;
            }
        }
        return 0;
    }

    private int countOccurrences(String text, String term) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(term, idx)) != -1) {
            count++;
            idx += term.length();
        }
        return count;
    }

    private static class ScoredDocument {
        final Document document;
        final double score;

        ScoredDocument(Document document, double score) {
            this.document = document;
            this.score = score;
        }
    }

    public int getDocumentCount() {
        return allDocuments.size();
    }
}