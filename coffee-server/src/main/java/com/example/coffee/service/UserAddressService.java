package com.example.coffee.service;

import com.example.coffee.dto.request.AddressSaveReq;
import com.example.coffee.entity.UserAddress;

import java.util.List;

public interface UserAddressService {

    List<UserAddress> list(Long userId);

    void save(Long userId, AddressSaveReq req);

    void update(Long userId, Long id, AddressSaveReq req);

    void delete(Long userId, Long id);

    void setDefault(Long userId, Long id);
}
