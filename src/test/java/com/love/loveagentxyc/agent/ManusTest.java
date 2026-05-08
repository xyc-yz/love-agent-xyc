package com.love.loveagentxyc.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ManusTest {
    @Resource
    private Manus manus;

    @Test
    void run() {
        String result = manus.run("我想在哈尔滨市里约会，生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
        System.out.println(result);
    }

}