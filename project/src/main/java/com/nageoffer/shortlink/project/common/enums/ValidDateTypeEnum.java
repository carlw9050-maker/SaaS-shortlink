package com.nageoffer.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
//lombok的注解，作用是为类的所有带有final的字段生成构造函数
public enum ValidDateTypeEnum {

    /**
     * 永久有效期
     */
    PERNAMENT(0),

    /**
     * 自定义有效期
     */
    CUSTOM(1);

    @Getter
    private final int type;
}
