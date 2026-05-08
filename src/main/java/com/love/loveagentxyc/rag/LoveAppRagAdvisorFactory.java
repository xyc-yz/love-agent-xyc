package com.love.loveagentxyc.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问的工厂
 */
public class LoveAppRagAdvisorFactory {

    /**
     * 创建自定义的 RAG 检索增强顾问
     *
     * @param vectorStore 向量存储
     * @param status      状态
     * @return 自定义的 RAG 检索增强顾问
     */
    public static RetrievalAugmentationAdvisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        if (status == null){
            DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .similarityThreshold(0.6) // 相似度阈值
                    .topK(5) // 返回文档数量
                    .build();
            return RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                    .build();
        }
        // 过滤特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        // 创建文档检索器
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) // 过滤条件
                .similarityThreshold(0.6) // 相似度阈值
                .topK(5) // 返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
