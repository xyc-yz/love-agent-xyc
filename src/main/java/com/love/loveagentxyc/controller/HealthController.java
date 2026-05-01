package com.love.loveagentxyc.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class HealthController {
    @Resource
    private ChatClient chatClient;
    @GetMapping("/health")
    public String health() {
        return "ok，ok";
    }

    @RequestMapping("/chat")
    public Flux<String> index() {
        return chatClient.prompt()
                .user("你好,你是谁")
                .stream()
                .content();
    }
}
