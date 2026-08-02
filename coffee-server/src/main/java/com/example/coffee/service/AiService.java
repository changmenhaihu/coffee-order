package com.example.coffee.service;

import com.example.coffee.dto.request.AiChatReq;
import com.example.coffee.dto.response.AiChatResp;

import java.util.List;

public interface AiService {

    AiChatResp chat(Long userId, AiChatReq req);

    List<String> getSuggestions();
}
