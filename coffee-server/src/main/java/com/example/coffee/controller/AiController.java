package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.dto.request.AiChatReq;
import com.example.coffee.dto.response.AiChatResp;
import com.example.coffee.service.AiService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public Result<AiChatResp> chat(@Valid @RequestBody AiChatReq req) {
        Long userId = UserContext.getUserId();
        return Result.success(aiService.chat(userId, req));
    }

    @GetMapping("/suggestions")
    public Result<List<String>> suggestions() {
        return Result.success(aiService.getSuggestions());
    }
}
