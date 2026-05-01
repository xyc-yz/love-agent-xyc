package com.love.loveagentxyc.demo.invok;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Demo01 implements CommandLineRunner {
    @Resource
    private ChatModel dashScopeChatModel;


    @Override
    public void run(String... args) throws Exception {
     AssistantMessage result = dashScopeChatModel.call(new Prompt("你好,我是肖有财"))
                .getResult()
                .getOutput();
     System.out.println(result.getText());
    }
}
