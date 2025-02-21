package com.ziio.imagehubbackend.api.imagesearch;

import com.ziio.imagehubbackend.api.imagesearch.model.ImageSearchResult;
import com.ziio.imagehubbackend.api.imagesearch.sub.GetImageListApi;
import com.ziio.imagehubbackend.api.imagesearch.sub.GetImagePageUrlApi;

import java.util.List;

public class ImageSearchApiFacade {
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImagePageUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://img0.baidu.com/it/u=804293955,3474432113&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=691";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
