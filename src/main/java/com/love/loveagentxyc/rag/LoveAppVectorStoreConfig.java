package com.love.loveagentxyc.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;


@Slf4j
@Configuration
public class LoveAppVectorStoreConfig {

    private static final int BATCH_SIZE = 10;

    /**
     * 分批插入文档（适配阿里云Embedding 10 条限制）
     */
    private void batchInsertDocuments(VectorStore vectorStore, List<Document> splitDocuments) {
//        List<Document> splitDocuments = tokenTextSplitter.splitCustomized(documents);
        for (int i = 0; i < splitDocuments.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, splitDocuments.size());
            List<Document> batch = splitDocuments.subList(i, end);
            vectorStore.add(batch);
            log.info("已插入批次：" + (i / BATCH_SIZE + 1) + "，条数：" + batch.size());
        }
    }

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024) // 通义DashScope固定1024维
                .initializeSchema(true) // 自动创建表
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .schemaName("public")
                .vectorTableName("love_document")
                .maxDocumentBatchSize(10000)
                .build();
    }

    @Bean
    public CommandLineRunner initVectorData(VectorStore pgVectorVectorStore) {
        return args -> {
            boolean isEmpty = isVectorStoreEmpty(pgVectorVectorStore);

            if (!isEmpty) {
                log.info("向量库不为空，请勿重复初始化数据！");
                return;
            }

            // 空库执行插入
            log.info("开始向量数据初始化...");
            List<Document> documents = loveAppDocumentLoader.loadDocuments();
            batchInsertDocuments(pgVectorVectorStore, documents);
            log.info("向量数据初始化完成！");
        };
    }

    private boolean isVectorStoreEmpty(VectorStore vectorStore) {
        try {
            // 搜索 1 条任意数据（Spring AI 原生方法）
            List<Document> resultList = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("test")
                            .topK(1)
                            .build()
            );
            // 搜索结果为空 = 表为空
            return resultList.isEmpty();
        } catch (Exception e) {
            // 表不存在（首次启动）→ 视为空库
            log.info("表不存在，请检查数据库配置！");
            return true;
        }
    }


}