package com.example.coffee.controller.admin;

import com.example.coffee.common.PageResult;
import com.example.coffee.common.Result;
import com.example.coffee.dto.request.ReplyFeedbackReq;
import com.example.coffee.dto.response.FeedbackResp;
import com.example.coffee.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/feedbacks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public Result<PageResult<FeedbackResp>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(feedbackService.list(page, size, status));
    }

    @PutMapping("/{id}")
    public Result<Void> reply(@PathVariable Long id, @RequestBody ReplyFeedbackReq req) {
        feedbackService.reply(id, req.getReply());
        return Result.success("回复成功", null);
    }
}
