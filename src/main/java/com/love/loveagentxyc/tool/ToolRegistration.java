package com.love.loveagentxyc.tool;

import jakarta.annotation.Resource;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Value("${search.apikey}")
    private String searchApiKey;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Bean
    public ToolCallback[] allTools() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        FileOperationTool fileOperationTool = new FileOperationTool();
        TerminateTool terminateTool = new TerminateTool();
        ToolCallback[] localTools = ToolCallbacks.from(
                terminateTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                pdfGenerationTool,
                fileOperationTool
        );

        ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();
        if (mcpTools != null && mcpTools.length > 0) {
            // 合并
            ToolCallback[] allTools = new ToolCallback[localTools.length + mcpTools.length];
            /**
             * System.arraycopy(src, srcPos, dest, destPos, length)
             * src:源数组，即数组要复制的源数组。
             * srcPos:源数组要复制的起始位置。
             * dest:目标数组，即数组要复制到的目标数组。
             * destPos:目标数组的复制起始位置。
             * length:复制的长度。
             */
            System.arraycopy(localTools, 0, allTools, 0, localTools.length);
            System.arraycopy(mcpTools, 0, allTools, localTools.length, mcpTools.length);
            return allTools;
        }

        return localTools;

    }
}
