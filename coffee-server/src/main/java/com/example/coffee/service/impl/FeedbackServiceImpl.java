package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.PageResult;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.request.FeedbackReq;
import com.example.coffee.dto.response.FeedbackResp;
import com.example.coffee.entity.Feedback;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.FeedbackMapper;
import com.example.coffee.mapper.SysUserMapper;
import com.example.coffee.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackMapper feedbackMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public void submit(Long userId, FeedbackReq req) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setContent(req.getContent());
        feedback.setImages(req.getImages());
        feedback.setContact(req.getContact());
        feedback.setStatus(0);
        feedbackMapper.insert(feedback);
    }

    @Override
    public PageResult<FeedbackResp> list(int page, int size, Integer status) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<Feedback>()
                .orderByDesc(Feedback::getCreateTime);
        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }

        Page<Feedback> pageResult = feedbackMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getTotal(), pageResult.getPages(),
                page, size, pageResult.getRecords().stream().map(f -> {
                    FeedbackResp resp = new FeedbackResp();
                    resp.setId(f.getId());
                    resp.setUserId(f.getUserId());
                    resp.setContent(f.getContent());
                    resp.setImages(f.getImages());
                    resp.setContact(f.getContact());
                    resp.setStatus(f.getStatus());
                    resp.setReply(f.getReply());
                    resp.setCreateTime(f.getCreateTime());
                    SysUser user = sysUserMapper.selectById(f.getUserId());
                    if (user != null) {
                        resp.setNickname(user.getNickname());
                    }
                    return resp;
                }).toList());
    }

    @Override
    @Transactional
    public void reply(Long id, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "反馈不存在");
        }
        feedback.setReply(reply);
        feedback.setStatus(1);
        feedbackMapper.updateById(feedback);
    }
}
