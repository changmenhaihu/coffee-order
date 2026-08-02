package com.example.coffee.service;

import com.example.coffee.dto.request.ChangePasswordReq;
import com.example.coffee.dto.request.LoginReq;
import com.example.coffee.dto.request.RegisterReq;
import com.example.coffee.dto.request.UpdateProfileReq;
import com.example.coffee.dto.response.LoginResp;
import com.example.coffee.dto.response.TokenResp;
import com.example.coffee.dto.response.UserInfoResp;

public interface AuthService {

    void sendCaptcha(String email, String type);

    LoginResp register(RegisterReq req);

    LoginResp login(LoginReq req);

    TokenResp refreshToken(String refreshToken);

    UserInfoResp getUserInfo(Long userId);

    UserInfoResp getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileReq req);

    void changePassword(Long userId, ChangePasswordReq req);
}
