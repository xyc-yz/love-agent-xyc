package com.love.loveagentxyc.app;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "我叫肖有财，单身，想找对象，但是没有对象，我喜欢美女";
        String result = loveApp.doChat(message , chatId);
//        System.out.println(result);
        System.out.println("=============================================================================");
        //第二轮
        message = "我叫什么";
        result = loveApp.doChat(message , chatId);
//        System.out.println(result);
        System.out.println("=============================================================================");
        //第三轮
        message = "我喜欢什么来着？";
        result = loveApp.doChat(message , chatId);
//        System.out.println(result);
        System.out.println("=============================================================================");

    }

    @Test
    void doChatReport() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "我叫肖有财，单身，想找对象，我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatReport(message, chatId);


    }
}