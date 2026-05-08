package com.love.loveagentxyc.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.love.loveagentxyc.advisor.LogAdvisor;
import com.love.loveagentxyc.advisor.ReReadingAdvisor;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.mongo.MongoChatMemoryRepository;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class Manus extends ToolCallAgent{
    private final DashScopeChatModel dashScopeChatModel;


    public Manus(ToolCallback[] availableTools, DashScopeChatModel dashScopeChatModel) {
        super(availableTools);
        this.setName("肖有财Manus");
        // 核心系统提示词
        String SYSTEM_PROMPT = """
                你是肖有财Manus，一款搭载强大工具调用能力的智能AI助手。
                
                你的核心原则："先核实，再作答"
                - 针对事实性问题（地点、天气、路线），**必须优先调用工具**
                - 针对创意类任务（写作、规划），可直接运用自身知识
                - 除非用户明确要求，否则一律使用中文回复
                
                可用工具分类：
                - 地图工具（maps_*）：处理所有地理位置相关查询
                - 搜索工具（searchBocha）：执行网络搜索
                - 文件工具（readFile/writeFile）：执行文件读写操作
                - PDF工具（generatePDF）：生成PDF文档
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

// 步骤决策提示词
        String NEXT_STEP_PROMPT = """
                你是一名搭载强大工具能力的贴心助手。请遵循以下决策流程：
                
                步骤 1 - 对用户查询进行分类：
                
                A. 地理位置/场所相关（餐厅、商铺、路线、天气）：
                   → 必须使用地图/搜索工具（maps_*、searchBocha）
                   → 严禁依赖训练数据中的地理位置信息
                
                B. 信息查询类（新闻、事实、时事、操作指南）：
                   → 应使用 searchBocha 获取最新信息
                   → 尤其是针对时效性强或特定的事实性问题
                
                C. 创意/建议类任务（写作、规划、情感支持、常规建议）：
                   → 可直接运用自身知识回答
                   → 仅在用户明确要求调研或数据时使用工具
                
                D. 文件/PDF操作：
                   → 根据需要使用 readFile/writeFile/generatePDF
                
                步骤 2 - 工具选择准则：
                - 地理位置查询：maps_geo → maps_around_search/maps_text_search
                - 实时信息查询：searchBocha
                - 计算/单位转换：使用对应工具
                - 除非任务真正完成，否则不要调用终止工具
                
                步骤 3 - 工具执行完成后：
                - 清晰汇总查询结果
                - 提供可执行的后续操作建议
                - 主动询问用户是否需要更多帮助
                
                重要提示：
                - 不要提前结束对话
                - 仅在用户的需求完全满足时，使用终止工具
                - 主动为用户提供额外协助
                重要约束规则：
                - 仅生成用户明确指定的文件格式。
                - 除非用户主动要求，否则不得创建备份文件（如 .txt 文件）。
                - 若用户要求生成PDF，只需生成PDF即可，无需额外多余操作。
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(
                        new ReReadingAdvisor()
//                        new LogAdvisor()
                )
                .build();
        this.setChatClient(chatClient);
        this.dashScopeChatModel = dashScopeChatModel;
    }
}
