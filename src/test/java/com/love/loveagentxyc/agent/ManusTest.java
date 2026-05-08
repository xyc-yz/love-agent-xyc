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
    void testMultiUser() {
        String userA = "user_001";
        String userB = "user_002";

        // 用户 A 问南京
        String q1 = manus.run("帮我找南京江宁区的约会地点", userA);
        System.out.println(q1);

        // 用户 B 问北京（互不干扰）
        String q2 = manus.run("帮我找北京朝阳区的餐厅", userB);
        System.out.println(q2);

        // 用户 A 继续追问（记忆还在）
        String q3 = manus.run("第一家店怎么走？", userA);
        System.out.println(q3);
    }

}