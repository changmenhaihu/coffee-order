package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.dto.request.CaptchaReq;
import com.example.coffee.dto.request.ChangePasswordReq;
import com.example.coffee.dto.request.LoginReq;
import com.example.coffee.dto.request.RegisterReq;
import com.example.coffee.dto.request.UpdateProfileReq;
import com.example.coffee.dto.response.LoginResp;
import com.example.coffee.dto.response.TokenResp;
import com.example.coffee.dto.response.UserInfoResp;
import com.example.coffee.service.AuthService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/captcha")
    public Result<Void> sendCaptcha(@Valid @RequestBody CaptchaReq req) {
        authService.sendCaptcha(req.getEmail(), req.getType());
        return Result.success("验证码已发送", null);
    }

    @PostMapping("/register")
    public Result<LoginResp> register(@Valid @RequestBody RegisterReq req) {
        return Result.success("注册成功", authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return Result.success("登录成功", authService.login(req));
    }

    @PostMapping("/refresh")
    public Result<TokenResp> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return Result.success("刷新成功", authService.refreshToken(token));
    }

    @GetMapping("/info")
    public Result<UserInfoResp> info() {
        Long userId = UserContext.getUserId();
        return Result.success(authService.getUserInfo(userId));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        authService.changePassword(UserContext.getUserId(), req);
        return Result.success("密码修改成功，请重新登录", null);
    }

    @GetMapping("/profile")
    public Result<UserInfoResp> profile() {
        return Result.success(authService.getProfile(UserContext.getUserId()));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileReq req) {
        authService.updateProfile(UserContext.getUserId(), req);
        return Result.success("个人资料已更新", null);
    }
}
