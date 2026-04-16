package com.xbatis.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserPageQuery {

    @Min(value = 1, message = "current 不能小于 1")
    private Integer current = 1;

    @Min(value = 1, message = "pageSize 不能小于 1")
    @Max(value = 100, message = "pageSize 不能超过 100")
    private Integer pageSize = 10;

    private String keyword;

    private Integer enabled;
}
