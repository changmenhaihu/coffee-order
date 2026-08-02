package com.example.coffee.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long total;
    private long pages;
    private long current;
    private long size;
    private List<T> list;   // 前端期望字段名为 list

    public static <T> PageResult<T> of(long total, long pages, long current, long size, List<T> list) {
        return new PageResult<>(total, pages, current, size, list);
    }
}