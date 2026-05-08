package com.love.loveagentxyc.tool;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博查AI搜索工具（完全对接 https://api.bocha.cn/ 官方接口）
 * 接口文档：https://bocha-ai.feishu.cn/wiki/RXEOw02rFiwzGSkd9mUcqoeAnNK
 */
public class WebSearchTool {

    // 博查核心搜索接口地址
    private static final String BOCHA_SEARCH_API_URL = "https://api.bochaai.com/v1/web-search";


    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }


    /**
     * 调用博查接口执行恋爱相关内容搜索
     * @param query 恋爱相关搜索关键词（必填）
     * @return 格式化后的前5条网页搜索结果，异常时返回友好提示
     */
    @Tool(description = "Search for love/relationship related information from Bocha AI (博查), support professional emotional guidance retrieval")
    public String searchBocha(
            @ToolParam(description = "Love/relationship related search query keyword (only support love related questions)") String query) {
        // 1. 构建请求头（鉴权+Content-Type）
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(Header.AUTHORIZATION.getValue(), "Bearer " + apiKey);
        headerMap.put(Header.CONTENT_TYPE.getValue(), "application/json");

        // 2. 构建请求体（按接口文档默认值配置）
        JSONObject requestBody = new JSONObject();
        requestBody.put("query", query);          // 搜索关键词（必填）
        requestBody.put("freshness", "noLimit");  // 时间范围：不限（推荐默认值）
        requestBody.put("summary", true);         // 显示文本摘要
        requestBody.put("count", 5);              // 返回5条结果（接口范围1-50）

        try {
            // 3. 发送POST请求调用博查API
            String responseStr = HttpRequest.post(BOCHA_SEARCH_API_URL)
                    .addHeaders(headerMap)
                    .body(requestBody.toString())
                    .execute()
                    .body();

            // 4. 解析响应结果
            JSONObject responseJson = JSONUtil.parseObj(responseStr);
            
            // 4.1 校验接口返回状态码
            int code = responseJson.getInt("code");
            if (code != 200) {
                String errMsg = responseJson.getStr("msg", "未知错误");
                return String.format("博查搜索失败：%s，有问题可以联系客服 ", errMsg);
            }

            // 4.2 提取核心网页结果（webPages.value）
            JSONObject dataObj = responseJson.getJSONObject("data");
            JSONObject webPagesObj = dataObj.getJSONObject("webPages");
            JSONArray webPageValues = webPagesObj.getJSONArray("value");

            // 4.3 处理无结果场景
            if (webPageValues == null || webPageValues.isEmpty()) {
                return "抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，有问题可以联系客服 ";
            }

            // 4.4 提取前5条结果并格式化
            List<Object> top5Results = webPageValues.size() > 5 
                    ? webPageValues.subList(0, 5) 
                    : webPageValues;
            
            String formattedResult = top5Results.stream().map(item -> {
                JSONObject pageObj = (JSONObject) item;
                return String.format(
                        "【标题】：%s\n【来源】：%s\n【链接】：%s\n【摘要】：%s\n",
                        pageObj.getStr("name", "无标题"),
                        pageObj.getStr("siteName", "未知网站"),
                        pageObj.getStr("url", "无链接"),
                        pageObj.getStr("snippet", "无摘要")
                );
            }).collect(Collectors.joining("-------------------------\n"));

            return formattedResult;

        } catch (Exception e) {
            // 5. 异常捕获（网络/解析/其他）
            return String.format("恋爱相关内容搜索出错：%s，有问题可以联系客服 肖有财", e.getMessage());
        }
    }
}