package com.example.coffee.dto.response;

import lombok.Data;

@Data
public class CategoryResp {

    private Long id;
    private Long storeId;
    private String name;
    private Integer sortOrder;
}
