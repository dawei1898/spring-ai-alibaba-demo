package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * 向量存储(Vector Store)
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class VectorStoreTest {

    public static final String MODEL_QWEN_PLUS = "qwen-plus";

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;


    @Resource
    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;




    /**
     * 搜索
     */
    @Test
    public void test01() {
        String message = "有什么手机？";
        System.out.println("【搜索内容】: " + message);

        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(apiKey).build();

        DashScopeCloudStore cloudStore = new DashScopeCloudStore(
                dashScopeApi, new DashScopeStoreOptions("百炼手机产品介绍"));

        List<Document> documentList = cloudStore.similaritySearch(message);
        System.out.println("【搜索结果】: " + documentList);
    }



}
