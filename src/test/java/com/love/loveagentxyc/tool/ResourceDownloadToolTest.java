package com.love.loveagentxyc.tool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ResourceDownloadToolTest {

    @Test
    void downloadResource() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String result = resourceDownloadTool.downloadResource("https://haowallpaper.com/link/common/file/previewFileImg/18834482406280576", "photo.png");
    }
}