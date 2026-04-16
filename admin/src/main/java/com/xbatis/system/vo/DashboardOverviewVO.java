package com.xbatis.system.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardOverviewVO {

    private int userCount;
    private int roleCount;
    private int menuCount;
    private int enabledUserCount;
}
