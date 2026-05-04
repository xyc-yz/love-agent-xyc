package com.love.loveagentxyc.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.aop.Advisor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示 Spring AI 调用阿里云大模型
 * 通过 CommandLineRunner 接口实现应用启动时自动执行
 */
@Component
public class SpringAiInvoke implements CommandLineRunner {
    // 注入 DashScope 聊天模型
    @Resource
    private ChatModel dashScopeChatModel;

    /**
     * run 方法会在 Spring Boot 应用启动完成后自动执行
     * 这是 CommandLineRunner 接口的特性
     *
     * @param args 命令行参数
     */
    @Override
    public void run(String... args) throws Exception {
        List<Message> prompt = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage("你是一个的ai助手，叫鱼皮");
        prompt.add(systemMessage);
        UserMessage userMessage = new UserMessage("你好,我是肖有财，你是谁");
        prompt.add(userMessage);
        // 构造 Prompt（提示词）对象并调用大模型
        AssistantMessage result = dashScopeChatModel
                .call(new Prompt(prompt))
                .getResult()        // 获取返回结果
                .getOutput();       // 获取 AI 回复消息

        // 打印 AI 的回复内容到控制台
        System.out.println(result.getText());
    }
}
