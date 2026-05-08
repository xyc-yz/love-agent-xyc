package com.love.loveagentxyc.agent;

import cn.hutool.core.util.StrUtil;
import com.love.loveagentxyc.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定义步骤，抽象的基础代理类
 * @author xyc
 * @date 2022/03/05
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
//    private List<Message> messageList = new ArrayList<>();

    // 1. 将单个 List 改为 Map，Key 是 conversationId
    private final Map<String, List<Message>> sessionMemories = new ConcurrentHashMap<>();

    // 2. 提供一个方法来获取指定用户的消息列表
    protected List<Message> getMessageList(String conversationId) {
        // 如果该用户没有记录，就新建一个线程安全的 List
        return sessionMemories.computeIfAbsent(conversationId, k -> Collections.synchronizedList(new ArrayList<>()));
    }

    protected void setSessionMemories(List<Message> messages, String conversationId) {
        if (StrUtil.isNotBlank(conversationId) && messages != null) {
            sessionMemories.put(conversationId, Collections.synchronizedList(new ArrayList<>(messages)));
        }
    }

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt, String conversationId) {
        // 【自动重置】如果上轮任务已结束或报错，自动重置状态，支持连续对话
        if (this.state == AgentState.FINISHED || this.state == AgentState.ERROR) {
            this.state = AgentState.IDLE;
        }

        // 1、基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("状态不能执行: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("未输入用户提示词");
        }

        // 2、执行，更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文到指定会话
        getMessageList(conversationId).add(new UserMessage(userPrompt));

        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("会话[{}] 步骤： {}/{}", conversationId, stepNumber, maxSteps);
                // 单步执行（传入 conversationId）
                step(conversationId);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
            }

            // 3、清理资源并返回最终结果
            this.cleanup();

            // 返回该会话最后一条 AI 的回复（过滤掉工具调用产生的中间 JSON）
            List<Message> history = getMessageList(conversationId);
            for (int i = history.size() - 1; i >= 0; i--) {
                if (history.get(i) instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                    return history.get(i).getText();
                }
            }
            return "执行完成";
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("智能体错误：", e);
            return "执行错误: " + e.getMessage();
        }
    }


    /**
     * 定义单个步骤
     *
     * @param conversationId 会话 ID
     * @return 步骤执行结果
     */
    public abstract String step(String conversationId);
    /**
     * 清理资源
     */
    protected void cleanup() {

    }

}