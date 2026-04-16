package com.xbatis.system.dto;

import cn.xbatis.db.annotations.Condition;
import cn.xbatis.db.annotations.ConditionTarget;
import cn.xbatis.db.annotations.Conditions;
import cn.xbatis.db.annotations.Ignore;
import com.xbatis.system.entity.SysMenu;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;


@Data
@ConditionTarget(SysMenu.class)
public class MenuPageQuery {

    @Ignore
    @Min(value = 1, message = "current 不能小于 1")
    private Integer current = 1;

    @Ignore
    @Min(value = 1, message = "pageSize 不能小于 1")
    @Max(value = 100, message = "pageSize 不能超过 100")
    private Integer pageSize = 10;

    @Conditions({
            @Condition(value = Condition.Type.ILIKE, property = SysMenu.Fields.name),
            @Condition(value = Condition.Type.ILIKE, property = SysMenu.Fields.path),
            @Condition(value = Condition.Type.ILIKE, property = SysMenu.Fields.permission),
            @Condition(value = Condition.Type.ILIKE, property = SysMenu.Fields.component),
            @Condition(value = Condition.Type.ILIKE, property = SysMenu.Fields.icon),
            @Condition(value = Condition.Type.ILIKE, property = SysMenu.Fields.remark)
    })
    private String keyword;

    private String type;

    private Integer status;
}
