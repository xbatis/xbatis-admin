package com.xbatis.commons.api;

import cn.xbatis.core.mybatis.mapper.context.Pager;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> records;
    private int current;
    private int pageSize;
    private int total;
    private int totalPages;

    public static <T> PageResponse<T> of(Pager<?> pager, List<T> records) {
        return PageResponse.<T>builder()
                .records(records)
                .current(pager.getNumber())
                .pageSize(pager.getSize())
                .total(pager.getTotal() == null ? 0 : pager.getTotal())
                .totalPages(pager.getTotalPage() == null ? 0 : pager.getTotalPage())
                .build();
    }
}
