package com.example.coffee.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDetailResp extends ProductListResp {

    private String storeName;
    private List<EvaluationResp> recentEvaluations;
}
