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
        return scored.stream()
                .limit(topK)
                .map(sd -> sd.document)
                .collect(Collectors.toList());
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
        return score;
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