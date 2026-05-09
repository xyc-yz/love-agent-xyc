package com.love.loveagentxyc.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.love.loveagentxyc.agent.Manus;
import com.love.loveagentxyc.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    /**
     * 同步调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
//    @GetMapping(value = "/love_app/chat/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
//    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
//        Flux<String> stringFlux = loveApp.doChatByStream(message, chatId);
//        System.out.println("stringFlux: " + stringFlux);
//        return stringFlux;
//    }


//    @GetMapping(value = "/love_app/ragchat/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
//    public Flux<String> doChatWithLoveAppRagSSE(String message, String chatId) {
//        Flux<String> stringFlux = loveApp.doChatByStreamByRag(message, chatId);
//        System.out.println("stringFlux: " + stringFlux);
//        return stringFlux;
//    }


    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/flux")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        loveApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    @GetMapping(value = "/love_app/chatRag/flux")
    public SseEmitter doChatRagWithLoveAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        loveApp.doChatByStreamByRag(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * SSE 流式调用 Manus 超级智能体（按步骤推送中间结果）
     *
     * @param message
     * @param conversationId 会话 ID
     */
    @GetMapping(value = "/manus/chat")
    public SseEmitter doChatWithManus(String message, String conversationId) {
        Manus manus = new Manus(allTools, (DashScopeChatModel) dashscopeChatModel);
        return manus.runStream(message, conversationId);
    }



}
