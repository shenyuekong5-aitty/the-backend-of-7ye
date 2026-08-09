package org.example.springboot2.ai;

import org.example.springboot2.ai.model.Document;
import org.example.springboot2.ai.service.DocumentIndexService;
import org.example.springboot2.ai.service.PromptBuilder;
import org.example.springboot2.ai.service.SessionHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
        assertTrue(publicDocs.isEmpty(), "Public should have no accessible documents since all resources are Owner");
    }

    @Test
    void testFriendRoleOnlyAccessesPublicAndFriend() {
        List<Document> friendDocs = documentIndexService.getDocumentsByRole("Friend");
        assertTrue(friendDocs.isEmpty(), "Friend should have no accessible documents since all resources are Owner");
    }

    @Test
    void testOwnerRoleAccessesAll() {
        List<Document> ownerDocs = documentIndexService.getDocumentsByRole("Owner");
        assertEquals(244, ownerDocs.size(), "Owner should have access to all 244 documents");
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