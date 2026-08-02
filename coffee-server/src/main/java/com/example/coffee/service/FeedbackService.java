package com.example.coffee.service;

import com.example.coffee.common.PageResult;
import com.example.coffee.dto.request.FeedbackReq;
import com.example.coffee.dto.response.FeedbackResp;

public interface FeedbackService {

    void submit(Long userId, FeedbackReq req);

    PageResult<FeedbackResp> list(int page, int size, Integer status);

    void reply(Long id, String reply);
}
