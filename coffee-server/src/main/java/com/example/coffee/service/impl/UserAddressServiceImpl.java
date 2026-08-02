package com.example.coffee.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.coffee.common.BusinessException;
import com.example.coffee.common.ResultCode;
import com.example.coffee.dto.request.AddressSaveReq;
import com.example.coffee.entity.UserAddress;
import com.example.coffee.mapper.UserAddressMapper;
import com.example.coffee.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> list(Long userId) {
        return userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getUpdateTime));
    }

    @Override
    @Transactional
    public void save(Long userId, AddressSaveReq req) {
        UserAddress addr = new UserAddress();
        addr.setUserId(userId);
        addr.setName(req.getName());
        addr.setPhone(req.getPhone());
        addr.setProvince(req.getProvince());
        addr.setCity(req.getCity());
        addr.setDistrict(req.getDistrict());
        addr.setDetail(req.getDetail());
        addr.setLatitude(req.getLatitude());
        addr.setLongitude(req.getLongitude());
        addr.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : 0);

        if (Integer.valueOf(1).equals(addr.getIsDefault())) {
            clearDefault(userId);
        }
        userAddressMapper.insert(addr);
    }

    @Override
    @Transactional
    public void update(Long userId, Long id, AddressSaveReq req) {
        UserAddress addr = userAddressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }
        addr.setName(req.getName());
        addr.setPhone(req.getPhone());
        addr.setProvince(req.getProvince());
        addr.setCity(req.getCity());
        addr.setDistrict(req.getDistrict());
        addr.setDetail(req.getDetail());
        addr.setLatitude(req.getLatitude());
        addr.setLongitude(req.getLongitude());

        if (req.getIsDefault() != null) {
            if (Integer.valueOf(1).equals(req.getIsDefault())) {
                clearDefault(userId);
            }
            addr.setIsDefault(req.getIsDefault());
        }
        userAddressMapper.updateById(addr);
    }

    @Override
    public void delete(Long userId, Long id) {
        UserAddress addr = userAddressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }
        userAddressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long id) {
        UserAddress addr = userAddressMapper.selectById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "地址不存在");
        }
        clearDefault(userId);
        addr.setIsDefault(1);
        userAddressMapper.updateById(addr);
    }

    private void clearDefault(Long userId) {
        List<UserAddress> defaults = userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getIsDefault, 1));
        for (UserAddress addr : defaults) {
            addr.setIsDefault(0);
            userAddressMapper.updateById(addr);
        }
    }
}
