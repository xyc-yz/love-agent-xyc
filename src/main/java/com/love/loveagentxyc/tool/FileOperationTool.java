package com.love.loveagentxyc.tool;

import cn.hutool.core.io.FileUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class FileOperationTool {

    private final String FILE_DIR = System.getProperty("user.dir") + "/tmp" + "/file";

    @Tool(description = "读文件内容")
    public String readFile(@ToolParam(description = "文件名字") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "读取失败: " + e.getMessage();
        }
    }

    @Tool(description = "写一个文件")
    public String writeFile(
        @ToolParam(description = "文件的名字") String fileName,
        @ToolParam(description = "写入文件的内容") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "文件写入成功: " + filePath;
        } catch (Exception e) {
            return "文件写入失败: " + e.getMessage();
        }
    }
}
