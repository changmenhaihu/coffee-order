package com.example.coffee.service.impl;

import com.example.coffee.dto.request.AiChatReq;
import com.example.coffee.dto.response.AiChatResp;
import com.example.coffee.service.AiService;
import com.example.coffee.util.RedisUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.model}")
    private String model;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String SYSTEM_PROMPT = """
        你是一家咖啡店的AI点单助手。你可以帮助用户：
        1. 推荐咖啡和饮品
        2. 协助下单（将商品加入购物车）
        3. 回答关于咖啡的问题
        4. 帮助用户修改购物车

        请用友好的语气回复，每次回复控制在2-3句话以内。
        如果用户想下单，请明确告诉用户你会帮他操作。

        你的回复必须是JSON格式：
        {"reply":"你的回复内容","action":null}

        当用户明确要执行操作时，action字段包含操作信息：
        - 推荐商品：{"type":"recommend"}
        - 加入购物车：{"type":"add_cart","productId":商品ID,"specId":规格ID,"quantity":数量}
        - 创建订单：{"type":"create_order"}
        - 清空购物车：{"type":"clear_cart"}
        如果不需要执行操作，action为null。
        """;

    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "来一杯提神醒脑的冰美式",
            "今天有什么热销推荐吗？",
            "我想要一杯拿铁，少冰",
            "推荐一个适合夏天的饮品",
            "帮我把购物车里的商品下单"
    );

    @Override
    public AiChatResp chat(Long userId, AiChatReq req) {
        String cacheKey = "ai:chat:" + md5(req.getMessage() + req.getStoreId());
        AiChatResp cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        AiChatResp resp;
        if (apiKey != null && !apiKey.isBlank() && !apiKey.equals("your-api-key")) {
            try {
                resp = callDeepSeekApi(req);
            } catch (Exception e) {
                log.warn("DeepSeek API调用失败，降级为本地规则匹配: {}", e.getMessage());
                resp = localRuleMatch(req.getMessage(), req.getStoreId());
            }
        } else {
            resp = localRuleMatch(req.getMessage(), req.getStoreId());
        }

        redisUtil.set(cacheKey, resp, 1, TimeUnit.HOURS);
        return resp;
    }

    @Override
    public List<String> getSuggestions() {
        return DEFAULT_SUGGESTIONS;
    }

    private AiChatResp callDeepSeekApi(AiChatReq req) {
        try {
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

            if (req.getHistory() != null) {
                for (var h : req.getHistory()) {
                    messages.add(Map.of("role", h.getOrDefault("role", "user"),
                            "content", h.getOrDefault("content", "")));
                }
            }

            messages.add(Map.of("role", "user", "content", req.getMessage()));

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", 0.7,
                    "max_tokens", 500,
                    "stream", false
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    String content = choices.get(0).get("message").get("content").asText();
                    content = extractJsonContent(content);
                    try {
                        return objectMapper.readValue(content, AiChatResp.class);
                    } catch (Exception e) {
                        log.debug("AI返回内容无法解析为JSON，作为纯文本回复: {}", content);
                        AiChatResp resp = new AiChatResp();
                        resp.setReply(content);
                        return resp;
                    }
                }
            } else {
                log.error("DeepSeek API返回错误: status={}, body={}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("DeepSeek API请求失败", e);
        }
        return localRuleMatch(req.getMessage(), req.getStoreId());
    }

    private String extractJsonContent(String raw) {
        raw = raw.trim();
        if (raw.startsWith("```json")) {
            raw = raw.substring(7);
        }
        if (raw.startsWith("```")) {
            int start = raw.indexOf('\n');
            if (start >= 0) raw = raw.substring(start + 1);
        }
        if (raw.endsWith("```")) {
            raw = raw.substring(0, raw.length() - 3);
        }
        return raw.trim();
    }

    private AiChatResp localRuleMatch(String message, Long storeId) {
        AiChatResp resp = new AiChatResp();

        if (message.contains("推荐") || message.contains("热销")) {
            resp.setReply("好的，为您推荐本店热销商品，请在推荐列表中查看。");
            AiChatResp.AiAction action = new AiChatResp.AiAction();
            action.setType("recommend");
            resp.setAction(action);
        } else if (message.contains("下单") || message.contains("结算") || message.contains("购买")) {
            resp.setReply("好的，正在为您创建订单，请确认订单信息。");
            AiChatResp.AiAction action = new AiChatResp.AiAction();
            action.setType("create_order");
            resp.setAction(action);
        } else if (message.contains("清空") && message.contains("购物车")) {
            resp.setReply("已为您清空购物车。");
            AiChatResp.AiAction action = new AiChatResp.AiAction();
            action.setType("clear_cart");
            resp.setAction(action);
        } else {
            resp.setReply("收到您的消息！您可以告诉我想要什么咖啡，我会帮您下单。或者试试说'推荐'、'清空购物车'等。");
        }

        return resp;
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
