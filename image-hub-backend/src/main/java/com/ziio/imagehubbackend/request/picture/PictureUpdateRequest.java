package com.ziio.imagehubbackend.request.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUpdateRequest implements Serializable {

    /**
     * 图片 id（用于修改）
     */
    private Long id;

    private String name;

    private String introduction;

    private String category;

    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
