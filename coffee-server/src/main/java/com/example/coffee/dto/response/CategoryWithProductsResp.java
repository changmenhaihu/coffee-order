package com.example.coffee.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CategoryWithProductsResp {

    private Long id;
    private String name;
    private List<ProductListResp> products;
}
