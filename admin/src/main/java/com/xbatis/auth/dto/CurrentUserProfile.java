package com.xbatis.auth.dto;

import com.xbatis.system.vo.MenuNodeVO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder
public class CurrentUserProfile {

    private Long userId;
    private String username;
    private String displayName;
    private Set<Long> roleIds;
    private Set<String> roleNames;
    private Set<String> permissions;
    private List<MenuNodeVO> menus;
}
