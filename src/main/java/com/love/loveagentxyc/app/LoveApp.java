package com.love.loveagentxyc.app;

import com.love.loveagentxyc.advisor.LogAdvisor;
import com.love.loveagentxyc.advisor.ReReadingAdvisor;
import com.love.loveagentxyc.rag.LoveAppVectorStoreConfig;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.mongo.MongoChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashScopeChatModel, MongoChatMemoryRepository mongoChatMemoryRepository) {
//        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();
//        ChatMemoryRepository mongoChatMemoryRepository = MongoChatMemoryRepository.builder()
//                .mongoTemplate(mongoTemplate)
//                .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                // 指定存储层：MongoDB
                .chatMemoryRepository(mongoChatMemoryRepository)
                // 可选：设置最多保留10条消息（默认20条）
                .maxMessages(10)
                .build();
        this.chatClient = ChatClient.builder(dashScopeChatModel)
                // 自动注册会话记忆增强器，无需手动new MessageChatMemoryAdvisor
                .defaultAdvisors(
                        new ReReadingAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new LogAdvisor()

                )
                // 你的系统提示词
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
    /**
     * 聊天
     */

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String text;
        if (chatResponse != null) {
            text = chatResponse.getResult().getOutput().getText();
            return text;
        }
        return "我无法理解你的问题，请重新提问";
    }

    /**
     * 聊天报告
     */
    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveReport doChatReport(String message, String chatId) {
        LoveReport report = chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("LoveReport: {}", report);
        return report;
    }

    @Resource
    private VectorStore loveAppVectorStore;

    /**
     *  检索增强
     * @param message
     * @param chatId
     * @return
     */

    public String doRAGChat(String message, String chatId) {

        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(loveAppVectorStore)
                        .build())
                .build();

        ChatResponse chatResponse = chatClient.prompt("给我解决方案，并推荐课程及其链接")
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .chatResponse();
        String text;
        if (chatResponse != null) {
            text = chatResponse.getResult().getOutput().getText();
            return text;
        }
        return "我无法理解你的问题，请重新提问";
    }
}
