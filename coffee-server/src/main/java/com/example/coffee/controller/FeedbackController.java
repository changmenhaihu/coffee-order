package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.dto.request.FeedbackReq;
import com.example.coffee.service.FeedbackService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public Result<Void> submit(@Valid @RequestBody FeedbackReq req) {
        feedbackService.submit(UserContext.getUserId(), req);
        return Result.success("反馈提交成功", null);
    }
}
