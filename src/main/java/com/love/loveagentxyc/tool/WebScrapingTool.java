package com.love.loveagentxyc.tool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {
    @Tool(description = "抓取网页内容")
    public String scrapeWebPage(@ToolParam(description = "待爬取网页的 URL") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "出现错误: " + e.getMessage();
        }
    }
}
