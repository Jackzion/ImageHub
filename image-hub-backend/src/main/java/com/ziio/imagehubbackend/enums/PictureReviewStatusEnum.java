package com.ziio.imagehubbackend.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("待审核",0),
    PASS("审核通过",1),
    REJECT("拒绝",2);

    private final String text;

    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureReviewStatusEnum getEnumByValue(int value){
        if(ObjectUtil.isEmpty(value)) return null;
        for(PictureReviewStatusEnum e : PictureReviewStatusEnum.values()){
            if(e.value == (value)) return e;
        }
        return null;
    }
}
