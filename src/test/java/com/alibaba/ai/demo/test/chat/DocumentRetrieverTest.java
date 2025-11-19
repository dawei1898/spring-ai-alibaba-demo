package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * 文档检索 (Document Retriever)
 *
 * @author muye
 */

@SpringBootTest
@ActiveProfiles("local")
public class DocumentRetrieverTest {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    /**
     * 查询阿里云百炼的知识库
     */
    @Test
    public void test01(){
        String message = "游戏玩家";
        System.out.println("提问: " + message);

        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(apiKey).build();

        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName("百炼手机产品介绍")//
                        .build());

        List<Document> documentList = documentRetriever.retrieve(new Query(message));

        System.out.println("documentList = " + documentList);
    }

}
