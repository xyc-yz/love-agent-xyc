package com.love.loveagentxyc.rag;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义关键词提取,基于 ai 模型
 */
@Component
public class MyKeyWordEnricher {
    @Resource
    private DashScopeChatModel dashScopeChatModel;

    @Bean
    public List<Document> enrich(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(dashScopeChatModel,5);
        List<Document> apply = enricher.apply(documents);
        return apply;
    }
}
