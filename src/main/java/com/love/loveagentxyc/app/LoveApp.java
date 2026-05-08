package com.love.loveagentxyc.app;

import com.love.loveagentxyc.advisor.LogAdvisor;
import com.love.loveagentxyc.advisor.ReReadingAdvisor;
import com.love.loveagentxyc.rag.LoveAppRagAdvisorFactory;
import com.love.loveagentxyc.utils.QueryRewriter;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.mongo.MongoChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Component
@Slf4j
public class LoveApp {
    @Resource
    QueryRewriter queryRewriter;

    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。只中文回复";
    private static final String SYSTEM_PROMPT_RAG = "你是婚恋与亲密关系方向的顾问，回答用户问题时必须遵守以下规则。【关于上下文】:" +
            "当用户消息中出现以「Context information is below」开头、或以明显分隔线标注的检索内容时，将其视为唯一可信的事实来源。请优先、且主要依据该部分内容作答。" +
            "【回答方式】\n" +
            "1. 先直接回应用户的问题（例如：修复思路、步骤、注意事项），用简洁的小标题或分点列出，便于阅读。" +
            "2. 若上下文中有推荐课程，请单独列出：课程名称、一句话简介（若上下文有）、完整链接（若上下文有）；不得省略或改写链接域名与路径。" +
            "3. 不要要求用户先说明「单身/恋爱/已婚」等状态，也不要用长篇开场问诊代替正文；除非上下文完全没有涉及用户处境所需的关键信息，否则只做一句以内的必要澄清。" +
            "4. 全文使用中文，语气专业、温和、可操作，避免空话套话。";

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
//                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    /**
     * 聊天
     */

    public String doChat(String message, String chatId) {
        String rewriteMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewriteMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .system(SYSTEM_PROMPT)
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
        String rewriteMessage = queryRewriter.doQueryRewrite(message);
        LoveReport report = chatClient.prompt()
                .user(rewriteMessage)
                .system(SYSTEM_PROMPT)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("LoveReport: {}", report);
        return report;
    }

    @Resource
    private VectorStore loveAppVectorStore;

    /**
     * 检索增强rag
     *
     * @param message
     * @param chatId
     * @return
     */

    public String doRAGChat(String message, String chatId) {
        message = message +"给我解决方案，并推荐课程及其链接";
        String rewriteMessage = queryRewriter.doQueryRewrite(message);
//        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .similarityThreshold(0.50)
//                        .vectorStore(loveAppVectorStore)
//                        .build())
//                .build();

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = LoveAppRagAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, null);
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewriteMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .advisors(retrievalAugmentationAdvisor)
                .system(SYSTEM_PROMPT_RAG)
                .call()
                .chatResponse();
        String text;
        if (chatResponse != null) {
            text = chatResponse.getResult().getOutput().getText();
            return text;
        }
        return "我无法理解你的问题，请重新提问";
    }


    public String doRAGChatByStatus(String message, String chatId, String status) {
        message = message +"给我解决方案，并推荐课程及其链接";
        String rewriteMessage = queryRewriter.doQueryRewrite(message);
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = LoveAppRagAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, status);
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewriteMessage)
                .system(SYSTEM_PROMPT_RAG)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
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
