package com.ziio.imagehubbackend.request.picture;

import lombok.Data;

@Data
public class PictureUploadByBatchRequest {

    private String searchText;

    private Integer count = 10;

    private String namePrefix;

}
