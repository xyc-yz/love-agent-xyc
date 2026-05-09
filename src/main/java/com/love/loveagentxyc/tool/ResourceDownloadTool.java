package com.love.loveagentxyc.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class ResourceDownloadTool {

    @Tool(description = "给一个url然后下载资源")
    public String downloadResource(@ToolParam(description = "资源下载路径url") String url, @ToolParam(description = "用于保存下载资源的文件名") String fileName) {
        String fileDir = System.getProperty("user.dir") + "/tmp" + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 使用 Hutool 的 downloadFile 方法下载资源
            HttpUtil.downloadFile(url, new File(filePath));
            return "资源保存成功。\n" + DownloadLinkBuilder.hintLine("download", fileName);
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
