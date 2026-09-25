package org.example.springboot2.ai.service;

import org.example.springboot2.ai.model.ChatRequest;
import org.example.springboot2.ai.model.ChatResponse;
import org.example.springboot2.ai.model.Document;
import org.example.springboot2.friend.service.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class AIChatService {

    private static final Logger log = LoggerFactory.getLogger(AIChatService.class);

    private final DocumentIndexService documentIndexService;
    private final PromptBuilder promptBuilder;
    private final SessionHistoryService sessionHistoryService;
    private final FriendService friendService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-chat}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:1500}")
    private int maxTokens;

    @Value("${ai.owner-user-id:1}")
    private String ownerUserId;

    private static final Pattern[] SENSITIVE_PATTERNS = {
            Pattern.compile("(?i)(password|pwd|passwd)\\s*[:=]\\s*\\S+"),
            Pattern.compile("(?i)(token|api[_-]?key|secret)\\s*[:=]\\s*\\S+"),
            Pattern.compile("\\b1[3-9]\\d{9}\\b"),
            Pattern.compile("(?i)(验证码|code|otp)\\s*[:=]\\s*\\S+"),
    };

    /** 无关键词续问标记，命中且当前检索为空时回退到上一轮资源类型 */
    private static final String[] CONTINUATION_MARKERS = {
            "再", "换", "继续", "再来", "还有", "另一", "别的", "下一", "另外", "其他", "跟刚才",
    };

    /** 每个会话已展示过的文档 ID（用于“再推荐一首”时给出未展示过的新条目） */
    private final Map<String, Set<String>> sessionShownIds = new ConcurrentHashMap<>();
    /** 每个会话最近一轮命中的资源类型 */
    private final Map<String, Set<String>> sessionLastTypes = new ConcurrentHashMap<>();

    private static final int MAX_SHOWN_IDS_PER_SESSION = 200;

    public AIChatService(DocumentIndexService documentIndexService,
                         PromptBuilder promptBuilder,
                         SessionHistoryService sessionHistoryService,
                         FriendService friendService) {
        this.documentIndexService = documentIndexService;
        this.promptBuilder = promptBuilder;
        this.sessionHistoryService = sessionHistoryService;
        this.friendService = friendService;
    }

    public ChatResponse chat(ChatRequest request, String userId) {
        String role = resolveRole(userId);
        String question = request.getQuestion();

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question 不能为空");
        }

        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = sessionHistoryService.createSessionId();
        }

        List<Document> allowedDocs = documentIndexService.getDocumentsByRole(role);
        List<Document> retrievedDocs = documentIndexService.search(allowedDocs, question, 5);

        if (retrievedDocs.isEmpty() && isContinuation(question)) {
            Set<String> lastTypes = sessionLastTypes.get(sessionId);
            if (lastTypes != null && !lastTypes.isEmpty()) {
                List<Document> followUp = documentIndexService.nextForTypes(
                        allowedDocs, lastTypes, sessionShownIds.getOrDefault(sessionId, Set.of()), 5);
                if (!followUp.isEmpty()) {
                    retrievedDocs = followUp;
                }
            }
        }

        recordShown(sessionId, retrievedDocs);

        String systemPrompt = promptBuilder.buildSystemPrompt(role, retrievedDocs);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        List<SessionHistoryService.Message> history = sessionHistoryService.getHistory(sessionId);
        for (SessionHistoryService.Message msg : history) {
            Map<String, String> m = new HashMap<>();
            m.put("role", "user".equals(msg.getRole()) ? "user" : "assistant");
            m.put("content", msg.getContent());
            messages.add(m);
        }
        messages.add(Map.of("role", "user", "content", question));

        String answer = callDeepSeek(messages);
        if (!"Owner".equals(role)) {
            answer = sanitizeAnswer(answer);
        }

        sessionHistoryService.saveHistory(sessionId, question, answer);

        return new ChatResponse(sessionId, answer);
    }

    private String callDeepSeek(List<Map<String, String>> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("缺少 DEEPSEEK_API_KEY 环境变量");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = baseUrl + "/chat/completions";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            if (response.getBody() == null) {
                throw new RuntimeException("模型返回为空");
            }
            List<Map> choices = (List<Map>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("模型未返回有效内容");
            }
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("DeepSeek API returned error status: {}", e.getStatusCode(), e);
            throw new RuntimeException("模型服务返回错误: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("DeepSeek API call failed", e);
            throw new RuntimeException("模型调用失败，请稍后重试", e);
        }
    }

    private String resolveRole(String userId) {
        if (userId == null) return "Public";
        if (userId.equals(ownerUserId)) return "Owner";
        try {
            Long uid = Long.parseLong(userId);
            if (friendService.isFriend(Long.parseLong(ownerUserId), uid)) {
                return "Friend";
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return "Public";
    }

    private boolean isContinuation(String question) {
        for (String marker : CONTINUATION_MARKERS) {
            if (question.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private void recordShown(String sessionId, List<Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }
        Set<String> types = new HashSet<>();
        Set<String> shownIds = sessionShownIds.computeIfAbsent(sessionId, k -> new LinkedHashSet<>());
        for (Document doc : docs) {
            types.add(doc.getType());
            shownIds.add(doc.getId());
        }
        while (shownIds.size() > MAX_SHOWN_IDS_PER_SESSION) {
            Iterator<String> it = shownIds.iterator();
            it.next();
            it.remove();
        }
        sessionLastTypes.put(sessionId, types);
    }

    private String sanitizeAnswer(String answer) {
        String result = answer;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            result = pattern.matcher(result).replaceAll("***");
        }
        return result;
    }
}