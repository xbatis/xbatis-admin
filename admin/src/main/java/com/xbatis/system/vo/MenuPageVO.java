package com.xbatis.system.vo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuPageVO {

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
    private Integer depth;
    private String parentPathLabel;
}
