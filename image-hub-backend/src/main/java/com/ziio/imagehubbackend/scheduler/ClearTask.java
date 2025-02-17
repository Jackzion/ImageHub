package com.ziio.imagehubbackend.scheduler;

import com.ziio.imagehubbackend.entity.Picture;
import com.ziio.imagehubbackend.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class ClearTask {

    @Resource
    PictureService pictureService;

    @Scheduled(cron = "0 0 0 */7 * ?") // 每7天执行一次
    public void executeTaskEverySevenDays() {
        List<Picture> list = pictureService.list();
        list.forEach(picture -> {
            pictureService.clearPictureFile(picture);
        });
        log.info(" 定时清理图片资源。。。 ");
    }
}