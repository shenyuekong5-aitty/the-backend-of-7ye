package org.example.springboot2.ai;

import org.example.springboot2.ai.model.Document;
import org.example.springboot2.ai.service.DocumentIndexService;
import org.example.springboot2.ai.service.PromptBuilder;
import org.example.springboot2.ai.service.SessionHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AIComponentTests {

    @Autowired
    private DocumentIndexService documentIndexService;

    @Autowired
    private PromptBuilder promptBuilder;

    @Autowired
    private SessionHistoryService sessionHistoryService;

    @Test
    void testDocumentsLoaded() {
        int count = documentIndexService.getDocumentCount();
        assertTrue(count > 0, "DocumentIndexService should have loaded documents");
        System.out.println("Loaded " + count + " documents");
    }

    @Test
    void testPublicRoleOnlyAccessesPublic() {
        List<Document> publicDocs = documentIndexService.getDocumentsByRole("Public");
        assertTrue(publicDocs.isEmpty(), "Public should have no accessible documents since resources are Friend or above");
    }

    @Test
    void testFriendRoleAccessesFriendResources() {
        List<Document> friendDocs = documentIndexService.getDocumentsByRole("Friend");
        assertFalse(friendDocs.isEmpty(), "Friend should have access to Friend-permission resources");
    }

    @Test
    void testOwnerRoleAccessesAll() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        assertEquals(documentIndexService.getDocumentCount(), ownerDocs.size(), "Owner should have access to all documents");
    }

    @Test
    void testSearchReturnsResults() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        List<Document> results = documentIndexService.search(ownerDocs, "起风了", 5);
        assertFalse(results.isEmpty(), "Search should find '起风了'");
        assertTrue(results.size() <= 5, "Search should return at most 5 results");
    }

    @Test
    void testSearchEmptyQuery() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        List<Document> results = documentIndexService.search(ownerDocs, "", 5);
        assertTrue(results.isEmpty(), "Empty query should return no results");
    }

    @Test
    void testSearchIntentFallbackMusic() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        List<Document> results = documentIndexService.search(ownerDocs, "推荐一首歌", 5);
        assertFalse(results.isEmpty(), "Intent query '推荐一首歌' should retrieve music docs for Owner");
        assertTrue(results.stream().allMatch(d -> "music".equals(d.getType())));
    }

    @Test
    void testSearchIntentFallbackGames() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        List<Document> results = documentIndexService.search(ownerDocs, "推荐个游戏", 5);
        assertFalse(results.isEmpty(), "Intent query '推荐个游戏' should retrieve games docs for Owner");
        assertTrue(results.stream().allMatch(d -> "games".equals(d.getType())));
    }

    @Test
    void testSearchIntentFallbackForFriend() {
        List<Document> friendDocs = documentIndexService.getDocumentsByRole("Friend");
        List<Document> results = documentIndexService.search(friendDocs, "推荐一首歌", 5);
        assertFalse(results.isEmpty(), "Friend should retrieve music docs via intent fallback");
        assertTrue(results.stream().allMatch(d -> "music".equals(d.getType())));
    }

    @Test
    void testSearchIntentFallbackPersonalLibrary() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        assertSearchType(ownerDocs, "你的人生信条是什么", "creed");
        assertSearchType(ownerDocs, "小烨说过什么名言", "quotes");
        assertSearchType(ownerDocs, "你有什么昵称", "nicknames");
        assertSearchType(ownerDocs, "你对朋友有什么看法", "cognition");
    }

    @Test
    void testSearchMixedTypesIncludesEachType() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        List<Document> results = documentIndexService.search(ownerDocs, "推荐一首歌和一本小说", 5);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(d -> "music".equals(d.getType())), "Should include at least one music doc");
        assertTrue(results.stream().anyMatch(d -> "books".equals(d.getType())), "Should include at least one books doc");
        assertTrue(results.size() <= 5);
    }

    @Test
    void testNextForTypesExcludesShownAndCycles() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        List<Document> first = documentIndexService.nextForTypes(ownerDocs, Set.of("music"), Set.of(), 5);
        assertFalse(first.isEmpty());
        assertTrue(first.stream().allMatch(d -> "music".equals(d.getType())));

        List<String> idsToExclude = first.stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        List<Document> second = documentIndexService.nextForTypes(ownerDocs, Set.of("music"), idsToExclude, 5);
        assertFalse(second.isEmpty(), "Should return new entries");
        assertTrue(second.stream().noneMatch(d -> idsToExclude.contains(d.getId())), "Should not repeat shown entries");

        Set<String> allIds = new HashSet<>();
        ownerDocs.stream().filter(d -> "music".equals(d.getType())).forEach(d -> allIds.add(d.getId()));
        List<Document> cycled = documentIndexService.nextForTypes(ownerDocs, Set.of("music"), allIds, 5);
        assertFalse(cycled.isEmpty(), "Should cycle back when all entries were shown");
    }

    private void assertSearchType(List<Document> ownerDocs, String query, String expectedType) {
        List<Document> results = documentIndexService.search(ownerDocs, query, 5);
        assertFalse(results.isEmpty(), "Intent query '" + query + "' should retrieve " + expectedType + " docs");
        assertTrue(results.stream().allMatch(d -> expectedType.equals(d.getType())),
                "Intent query '" + query + "' should only retrieve " + expectedType + " docs");
    }

    @Test
    void testSystemPromptBuildsForPublic() {
        String prompt = promptBuilder.buildSystemPrompt("Public", List.of());
        assertNotNull(prompt);
        assertTrue(prompt.contains("role = Public"));
        assertTrue(prompt.contains("【回答要求】"));
    }

    @Test
    void testSystemPromptBuildsForOwner() {
        String prompt = promptBuilder.buildSystemPrompt("Owner", List.of());
        assertNotNull(prompt);
        assertTrue(prompt.contains("role = Owner"));
    }

    @Test
    void testCropByRoleRemovesOwnerOnly() {
        String content = "## Public Content\n\nsome text\n\n## Owner Only\n\nsecret text";
        String cropped = promptBuilder.cropByRole(content, "Public");
        assertTrue(cropped.contains("Public Content"));
        assertFalse(cropped.contains("Owner Only"));
        assertFalse(cropped.contains("secret text"));
    }

    @Test
    void testCropByRoleKeepsOwnerOnlyForOwner() {
        String content = "## Public Content\n\nsome text\n\n## Owner Only\n\nsecret text";
        String cropped = promptBuilder.cropByRole(content, "Owner");
        assertTrue(cropped.contains("Public Content"));
        assertTrue(cropped.contains("Owner Only"));
        assertTrue(cropped.contains("secret text"));
    }

    @Test
    void testSessionHistory() {
        String sessionId = sessionHistoryService.createSessionId();
        assertNotNull(sessionId);

        sessionHistoryService.saveHistory(sessionId, "你好", "你好！");
        List<SessionHistoryService.Message> history = sessionHistoryService.getHistory(sessionId);
        assertEquals(2, history.size());
        assertEquals("user", history.get(0).getRole());
        assertEquals("你好", history.get(0).getContent());
        assertEquals("assistant", history.get(1).getRole());
        assertEquals("你好！", history.get(1).getContent());
    }

    @Test
    void testSessionHistoryWithMultipleTurns() {
        String sessionId = sessionHistoryService.createSessionId();
        for (int i = 0; i < 6; i++) {
            sessionHistoryService.saveHistory(sessionId, "q" + i, "a" + i);
        }
        List<SessionHistoryService.Message> history = sessionHistoryService.getHistory(sessionId);
        assertEquals(12, history.size());
    }

    @Test
    void testDocumentAccessibleBy() {
        Document doc = new Document();
        doc.setPermission("Public");
        assertTrue(doc.isAccessibleBy("Public"));
        assertTrue(doc.isAccessibleBy("Friend"));
        assertTrue(doc.isAccessibleBy("Owner"));

        doc.setPermission("Friend");
        assertFalse(doc.isAccessibleBy("Public"));
        assertTrue(doc.isAccessibleBy("Friend"));
        assertTrue(doc.isAccessibleBy("Owner"));

        doc.setPermission("Owner");
        assertFalse(doc.isAccessibleBy("Public"));
        assertFalse(doc.isAccessibleBy("Friend"));
        assertTrue(doc.isAccessibleBy("Owner"));
    }
}