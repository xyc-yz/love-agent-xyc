package com.love.loveagentxyc.tool;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博查AI搜索工具（仅返回百度、哔哩哔哩、知乎、小黑盒内容）
 */
public class WebSearchTool {

    // 博查核心搜索接口地址
    private static final String BOCHA_SEARCH_API_URL = "https://api.bochaai.com/v1/web-search";
    // 目标平台域名列表
    private static final List<String> ALLOWED_DOMAINS = List.of(
            "baidu.com",    // 百度
            "bilibili.com", // 哔哩哔哩
            "zhihu.com",    // 知乎
            "bbs.hupu.com"  //虎扑
    );

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 调用博查接口执行恋爱相关内容搜索（仅返回百度/哔哩哔哩/知乎/小黑盒内容）
     * @param query 恋爱相关搜索关键词
     * @return 格式化后的前5条目标平台搜索结果，异常时返回提示
     */
    @Tool(description = "检索恋爱 / 情感相关信息，支持专业情感指导类内容的检索（仅返回百度、哔哩哔哩、知乎、小黑盒内容）")
    public String searchBocha(
            @ToolParam(description = "恋爱 / 情感相关的搜索查询关键词（仅支持恋爱相关问题）") String query) {

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(Header.AUTHORIZATION.getValue(), "Bearer " + apiKey);
        headerMap.put(Header.CONTENT_TYPE.getValue(), "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("query", query);          // 搜索关键词（必填）
        requestBody.put("freshness", "noLimit");  // 时间范围：不限
        requestBody.put("summary", true);         // 显示文本摘要
        requestBody.put("count", 10);             // 扩大请求数量（避免过滤后无结果），后续筛选前5条
        requestBody.put("include", String.join("|", ALLOWED_DOMAINS));

        try {
            // 发送POST请求调用博查API
            String responseStr = HttpRequest.post(BOCHA_SEARCH_API_URL)
                    .addHeaders(headerMap)
                    .body(requestBody.toString())
                    .execute()
                    .body();

            // 解析响应结果
            JSONObject responseJson = JSONUtil.parseObj(responseStr);

            // 校验接口返回状态码
            int code = responseJson.getInt("code");
            if (code != 200) {
                String errMsg = responseJson.getStr("msg", "未知错误");
                return String.format("博查搜索失败，有问题可以联系客服： "+errMsg);
            }

            // 提取核心网页结果
            JSONObject dataObj = responseJson.getJSONObject("data");
            if (dataObj == null) {
                return "抱歉，未查询到恋爱相关内容，有问题可以联系客服 ";
            }
            JSONObject webPagesObj = dataObj.getJSONObject("webPages");
            if (webPagesObj == null) {
                return "抱歉，未查询到恋爱相关内容，有问题可以联系客服 ";
            }
            JSONArray webPageValues = webPagesObj.getJSONArray("value");

            // 处理无结果场景
            if (webPageValues == null || webPageValues.isEmpty()) {
                return "抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，有问题可以联系客服 ";
            }

            // 方案2：代码层面过滤（API过滤失效时兜底）
            List<Object> filteredResults = new ArrayList<>();
            for (Object item : webPageValues) {
                JSONObject pageObj = (JSONObject) item;
                String url = pageObj.getStr("url", "");
                // 校验域名是否在允许列表中
                if (isAllowedDomain(url)) {
                    filteredResults.add(item);
                    // 最多保留5条结果
                    if (filteredResults.size() >= 5) {
                        break;
                    }
                }
            }

            // 处理过滤后无结果的场景
            if (filteredResults.isEmpty()) {
                return "抱歉，未查询到相关恋爱内容，有问题可以联系客服 ";
            }

            // 格式化过滤后的结果（仅用换行拼接）
            String formattedResult = filteredResults.stream().map(item -> {
                JSONObject pageObj = (JSONObject) item;
                return String.format(
                        "【标题】：%s\n【来源】：%s\n【链接】：%s\n【摘要】：%s......\n",
                        pageObj.getStr("name", "无标题"),
                        pageObj.getStr("siteName", "未知网站"),
                        pageObj.getStr("url", "无链接"),
                        pageObj.getStr("snippet", "无摘要")
                );
            }).collect(Collectors.joining("\n"));

            return formattedResult;

        } catch (Exception e) {
            return String.format("恋爱相关内容搜索出错：%s，有问题可以联系客服 肖有财", e.getMessage());
        }
    }

    /**
     * 校验URL的域名是否在允许列表中
     * @param url 网页链接
     * @return true=允许的域名，false=非目标域名
     */
    private boolean isAllowedDomain(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        // 统一转为小写，避免大小写问题
        String lowerUrl = url.toLowerCase();
        // 校验是否包含目标域名
        for (String domain : ALLOWED_DOMAINS) {
            if (lowerUrl.contains(domain.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}