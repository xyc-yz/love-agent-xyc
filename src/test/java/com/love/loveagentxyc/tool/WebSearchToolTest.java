package com.love.loveagentxyc.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class WebSearchToolTest {

    @Value("${search.apikey}")
    private  String apiKey;
    @Test
    void searchBocha() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String result = webSearchTool.searchBocha("吵架分手");
        System.out.println(result);

    }
}