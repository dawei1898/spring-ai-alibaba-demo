package com.alibaba.ai.demo.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 检索增强生成RAG 配置
 *
 * @author dawei
 */
@Configuration
public class RagConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {

        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 生成一个机器人产品说明书的文档
        List<Document> documents = List.of(new Document(
                """
                        产品说明书:
                        产品名称：智能机器人
                        产品描述：智能机器人是一个智能设备，能够自动完成各种任务。
                        功能：
                        1. 自动导航：机器人能够自动导航到指定位置。
                        2. 自动抓取：机器人能够自动抓取物品。
                        3. 自动放置：机器人能够自动放置物品。
                        """
        ));

        simpleVectorStore.add(documents);
        return  simpleVectorStore;
    }
}
