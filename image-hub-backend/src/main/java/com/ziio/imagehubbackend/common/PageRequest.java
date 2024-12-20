package com.ziio.imagehubbackend.common;

import lombok.Data;

@Data
public class PageRequest {

    private long current = 1;

    private long pageSize = 10;

    private String sortField;

    private String sortOrder = "descend";
}
