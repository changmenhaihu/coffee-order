package com.example.coffee.dto.response;

import lombok.Data;

@Data
public class AiChatResp {

    private String reply;
    private AiAction action;

    @Data
    public static class AiAction {
        private String type;
        private Object params;
    }
}
