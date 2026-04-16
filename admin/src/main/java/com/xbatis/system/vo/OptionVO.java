package com.xbatis.system.vo;

import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.xbatis.system.entity.SysRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ResultEntity(SysRole.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionVO {

    @ResultEntityField(property = "id")
    private Long value;

    @ResultEntityField(property = "name")
    private String label;
}
