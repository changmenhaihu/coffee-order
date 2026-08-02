package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.CaptchaLimitException;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.request.ChangePasswordReq;
import com.example.coffee.dto.request.LoginReq;
import com.example.coffee.dto.request.RegisterReq;
import com.example.coffee.dto.request.UpdateProfileReq;
import com.example.coffee.dto.response.LoginResp;
import com.example.coffee.dto.response.TokenResp;
import com.example.coffee.dto.response.UserInfoResp;
import com.example.coffee.entity.SysLoginLog;
import com.example.coffee.entity.SysUser;
import com.example.coffee.mapper.SysLoginLogMapper;
import com.example.coffee.mapper.SysUserMapper;
import com.example.coffee.service.AuthService;
import com.example.coffee.service.EmailService;
import com.example.coffee.util.JwtUtil;
import com.example.coffee.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String CAPTCHA_KEY = "captcha:%s:%s";
    private static final long CAPTCHA_TTL = 300;
    private static final long CAPTCHA_RESEND = 60;
    private static final String LOGIN_FAIL_COUNT = "login:fail:%s";
    private static final int MAX_FAIL_COUNT = 3;
    private static final long LOCK_DURATION = 180;

    private final SysUserMapper sysUserMapper;
    private final SysLoginLogMapper sysLoginLogMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void sendCaptcha(String email, String type) {
        String rateKey = "captcha:rate:" + type + ":" + email;
        if (redisUtil.exists(rateKey)) {
            long remain = redisUtil.getExpire(rateKey);
            throw new CaptchaLimitException((int) remain);
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = String.format(CAPTCHA_KEY, type, email);
        redisUtil.set(key, code, CAPTCHA_TTL, TimeUnit.SECONDS);
        redisUtil.set(rateKey, "1", CAPTCHA_RESEND, TimeUnit.SECONDS);
        emailService.sendCaptchaEmail(email, code);
    }

    @Override
    @Transactional
    public LoginResp register(RegisterReq req) {
        // 校验验证码
        String key = String.format(CAPTCHA_KEY, "register", req.getEmail());
        String cachedCode = redisUtil.get(key);
        if (cachedCode == null || !cachedCode.equals(req.getCaptcha())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }
        // 检查用户名唯一性
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername())) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : "用户" + System.currentTimeMillis() % 10000);
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        // 删除验证码
        redisUtil.delete(key);
        // 生成Token并返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        return new LoginResp(token, refreshToken, jwtUtil.getExpiration(), toUserInfo(user));
    }

    @Override
    @Transactional
    public LoginResp login(LoginReq req) {
        // 登录失败计数依赖 Redis，Redis 不可用时降级为不做次数限制，避免登录被 500 阻塞
        int failCount = 0;
        try {
            String failCountStr = redisUtil.get(String.format(LOGIN_FAIL_COUNT, req.getUsername()));
            failCount = failCountStr != null ? Integer.parseInt(failCountStr) : 0;
        } catch (Exception e) {
            log.warn("Redis不可用，跳过登录失败计数: {}", e.getMessage());
        }
        if (failCount >= MAX_FAIL_COUNT) {
            long remain = 0;
            try {
                remain = redisUtil.getExpire(String.format(LOGIN_FAIL_COUNT, req.getUsername()));
            } catch (Exception ignored) {
            }
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "账号已锁定，请" + (remain > 0 ? remain : 0) + "秒后重试");
        }
        // 查询用户（用 selectByUsername 绕过 @TableField(select=false) 拿到 password）
        SysUser user = sysUserMapper.selectByUsername(req.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        // 验证码登录
        if ("captcha".equals(req.getLoginType())) {
            String key = String.format(CAPTCHA_KEY, "login", req.getUsername());
            String cachedCode = redisUtil.get(key);
            if (cachedCode == null || !cachedCode.equals(req.getCaptcha())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
            }
            redisUtil.delete(key);
        } else {
            // 密码登录
            if (req.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                failCount++;
                int remain = MAX_FAIL_COUNT - failCount;
                if (remain <= 0) {
                    try {
                        redisUtil.set(String.format(LOGIN_FAIL_COUNT, req.getUsername()),
                                String.valueOf(failCount), LOCK_DURATION, TimeUnit.SECONDS);
                    } catch (Exception ignored) {
                    }
                    throw new BusinessException(ResultCode.FORBIDDEN,
                            "账号已锁定，请" + LOCK_DURATION + "秒后重试");
                }
                try {
                    redisUtil.set(String.format(LOGIN_FAIL_COUNT, req.getUsername()),
                            String.valueOf(failCount), LOCK_DURATION, TimeUnit.SECONDS);
                } catch (Exception ignored) {
                }
                throw new BusinessException(ResultCode.UNAUTHORIZED,
                        "密码错误，还剩" + remain + "次机会");
            }
            try {
                redisUtil.delete(String.format(LOGIN_FAIL_COUNT, req.getUsername()));
            } catch (Exception ignored) {
            }
        }
        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        // 记录登录日志
        recordLoginLog(user.getId(), user.getUsername(), 1, "登录成功");
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        return new LoginResp(token, refreshToken, jwtUtil.getExpiration(), toUserInfo(user));
    }

    @Override
    public TokenResp refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "Token无效或已过期");
        }
        Long userId = jwtUtil.getUserId(refreshToken);
        String username = jwtUtil.getUsername(refreshToken);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        return new TokenResp(newToken, newRefreshToken, jwtUtil.getExpiration());
    }

    @Override
    public UserInfoResp getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return toUserInfo(user);
    }

    @Override
    public UserInfoResp getProfile(Long userId) {
        return getUserInfo(userId);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UpdateProfileReq req) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordReq req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "两次密码不一致");
        }
        SysUser user = sysUserMapper.selectByIdWithPassword(userId);
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "旧密码错误");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码不能与旧密码相同");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Async
    public void recordLoginLog(Long userId, String username, int status, String msg) {
        try {
            SysLoginLog log = new SysLoginLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setStatus(status);
            log.setMsg(msg);
            log.setCreateTime(LocalDateTime.now());
            sysLoginLogMapper.insert(log);
        } catch (Exception e) {
            // 登录日志写入失败不影响登录主流程
            log.warn("登录日志写入失败: {}", e.getMessage());
        }
    }

    private UserInfoResp toUserInfo(SysUser user) {
        UserInfoResp resp = new UserInfoResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setPhone(user.getPhone());
        resp.setEmail(user.getEmail());
        resp.setGender(user.getGender());
        resp.setBalance(user.getBalance());
        resp.setRole(user.getRole());
        resp.setStoreId(user.getStoreId());
        resp.setCreateTime(user.getCreateTime());
        return resp;
    }
}
