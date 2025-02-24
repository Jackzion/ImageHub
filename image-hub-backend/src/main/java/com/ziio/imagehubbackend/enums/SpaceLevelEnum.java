package com.ziio.imagehubbackend.enums;

import lombok.Getter;

@Getter
public enum SpaceLevelEnum {

    /**
     * 100 张 ，100 M
     */
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    /**
     * 1000 张 ，1000 M
     */
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    /**
     * 10000 张 ，10000 M
     */
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;

    private final int value;

    private final long maxCount;

    private final long maxSize;

     SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    public static SpaceLevelEnum getEnumByValue(Integer spaceLevel) {

        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.getValue() == spaceLevel) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
