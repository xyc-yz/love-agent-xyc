package com.love.loveagentxyc.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

@Slf4j
public class LogAdvisor implements BaseAdvisor {

    /**
     * 请求发送给AI之前打印日志
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        this.logRequest(chatClientRequest);
        // 直接返回请求，框架自动链式传递
        return chatClientRequest;
    }

    /**
     * AI返回响应之后打印日志
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 增加空值判断，防止空指针报错
        if (chatClientResponse != null && chatClientResponse.chatResponse() != null) {
            this.logResponse(chatClientResponse);
        }
        // 直接返回响应
        return chatClientResponse;
    }

    /**
     * 打印请求日志
     */
    protected void logRequest(ChatClientRequest request) {
        log.info("AI Request：{}", request.prompt().getUserMessages());
    }

    /**
     * 打印响应日志
     */
    protected void logResponse(ChatClientResponse chatClientResponse) {
        log.info("AI Response：{}", chatClientResponse.chatResponse().getResult().getOutput().getText());
    }

    /**
     * 顾问名称
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    /**
     * 执行顺序（数字越小越先执行）
     */
    @Override
    public int getOrder() {
        return 1; // 调整为1，让ReReadingAdvisor先执行，再打印日志
    }
}