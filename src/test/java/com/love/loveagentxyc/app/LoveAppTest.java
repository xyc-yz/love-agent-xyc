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

    @Test
    void doRAGChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "Java是什么";
        String result = loveApp.doRAGChat(message, chatId);
    }

    @Test
    void doRAGChatByStatus() {
        String chatId = UUID.randomUUID().toString();
        String message = "我叫肖有财，已婚，婚后夫妻情感不合";
        String result = loveApp.doRAGChatByStatus(message, chatId, "已婚");
        System.out.println( result);
//        String result = loveApp.doRAGChatByStatus(message, chatId);
    }

    @Test
    void doChatWithTools() {
        String chatId = UUID.randomUUID().toString();
        String message = "生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单";
        String result = loveApp.doChatWithTools(message, chatId);
        System.out.println( result);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "我的另一半住在江宁区，翠屏东南，请帮我找到5公里内适合的最近的约会地点";
        String result = loveApp.doChatWithMcp(message, chatId);
        System.out.println( result);
    }
}