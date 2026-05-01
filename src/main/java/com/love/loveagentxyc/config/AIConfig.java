package com.love.loveagentxyc.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean
    public ChatClient chatClient(DashScopeChatModel dashScopeChatModel) {
       return ChatClient.builder(dashScopeChatModel)
               .defaultSystem("You are a helpful assistant.你是肖有财")
                .build();
    }
}
