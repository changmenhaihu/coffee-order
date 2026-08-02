package com.example.coffee.controller;

import com.example.coffee.common.Result;
import com.example.coffee.dto.request.AddressSaveReq;
import com.example.coffee.entity.UserAddress;
import com.example.coffee.service.UserAddressService;
import com.example.coffee.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    public Result<List<UserAddress>> list() {
        return Result.success(userAddressService.list(UserContext.getUserId()));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody AddressSaveReq req) {
        userAddressService.save(UserContext.getUserId(), req);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressSaveReq req) {
        userAddressService.update(UserContext.getUserId(), id, req);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userAddressService.delete(UserContext.getUserId(), id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        userAddressService.setDefault(UserContext.getUserId(), id);
        return Result.success();
    }
}
