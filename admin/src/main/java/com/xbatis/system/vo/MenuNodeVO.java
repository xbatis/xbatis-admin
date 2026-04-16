package com.xbatis.system.vo;

import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultField;
import com.xbatis.system.entity.SysMenu;
import lombok.*;

import java.util.List;

@ResultEntity(SysMenu.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuNodeVO {

    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String icon;
    private String permission;
    private String redirect;
    private Integer sort;
    private Integer visible;
    private Integer keepAlive;
    private Integer status;
    private String remark;

    @ResultField
    private List<MenuNodeVO> children;
}
