package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * 检索增强生成 RAG（Retrieval-Augmented Generation）
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class RAGTest {

    public static final String MODEL_QWEN_PLUS = "qwen-plus";


    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;


    /**
     * 检索增强服务
     */
    @Test
    public void test01() {
        String message = "请介绍机器人？";
        System.out.println("【提问】: " + message);

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_PLUS)
                .build();

        // 添加QuestionAnswerAdvisor并提供对应的向量存储，可以将之前放入的文档作为参考资料，并生成增强回答。
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);

        String content = chatClient.prompt(message)
                .options(chatOptions)
                .advisors(questionAnswerAdvisor)
                .call()
                .content();
        System.out.println("【回答】: " + content);
    }

    /**
     * 多查询扩展（Multi Query Expansion）
     */
    @Test
    public void test02() {
        // 设置系统提示信息，定义AI助手作为专业的室内设计顾问角色
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem("你是一位专业的室内设计顾问，精通各种装修风格、材料选择和空间布局。" +
                        "请基于提供的参考资料，为用户提供专业、详细且实用的建议。在回答时，请注意：\n" +
                        "1. 准确理解用户的具体需求\n" +
                        "2. 结合参考资料中的实际案例\n" +
                        "3. 提供专业的设计理念和原理解释\n" +
                        "4. 考虑实用性、美观性和成本效益\n" +
                        "5. 如有需要，可以提供替代方案");


        String message = "请提供几种推荐的装修方案？";
        System.out.println("【提问】: " + message);

        // 构建查询扩展器
        MultiQueryExpander multiQueryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                .includeOriginal(false) // 不包含原始查询
                .numberOfQueries(3) // 生成 3 个查询变体
                .build();

        // 执行查询扩展，将一个问题变为多个相关的查询
        List<Query> queryList = multiQueryExpander.expand(new Query(message));
        for (Query query : queryList) {
            System.out.println("【查询扩展后】: " + query.text());
        }
    }


    /**
     * 查询重写（Query Rewrite）
     */
    @Test
    public void test03() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 创建查询重写转换器
        QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();

        // 查询提问
        Query query = new Query("我在学习人工智能，请问什么是大语言模型？");
        System.out.println("原来提问 = " + query.text());

        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);

        System.out.println("查询重写后 = " + transformedQuery.text());

    }

    /**
     * 查询翻译（Query Translation）
     */
    @Test
    public void test04() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 创建查询翻译转换器
        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetLanguage("chinese") // 目标语言文中文
                .build();

        // 英文查询
        Query query = new Query("What is LLM ？");
        System.out.println("英文提问 = " + query.text());

        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);

        System.out.println("翻译后的提问 = " + transformedQuery.text());
    }

    /**
     * 上下文感知查询（Context-aware Queries）
     */
    @Test
    public void test05() {
        ChatClient.Builder builder = ChatClient.builder(chatModel);

        // 创建带上下文的查询转换器
        CompressionQueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();

        // 带上下文查询
        Query query = Query.builder()
                .history(new UserMessage("深圳市南山区的碧海湾小区在哪里?"),
                        new AssistantMessage("碧海湾小区位于深圳市南山区后海中心区，临近后海地铁站。")
                ) // 历史提问
                .text("那这个小区的二手房均价是多少?") // 当前提问
                .build();
        System.out.println("原来提问 = " + query.text());

        // 执行查询, 将提问的 ‘那这个小区’ 改为 ‘深圳市南山区的碧海湾小区’
        Query transformedQuery = queryTransformer.transform(query);

        System.out.println("替换后的提问 = " + transformedQuery.text());

    }

    /**
     * 检索增强顾问（RetrievalAugmentationAdvisor）
     */
    @Test
    public void test06() {
        // 初始化向量存储
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 添加文档到向量存储
        List<Document> documents = List.of(new Document(
                """
                        产品说明书:产品名称：智能机器人\n
                        产品描述：智能机器人是一个智能设备，能够自动完成各种任务。\n
                        功能：
                            1. 自动导航：机器人能够自动导航到指定位置。\n
                            2. 自动抓取：机器人能够自动抓取物品。\n
                            3. 自动放置：机器人能够自动放置物品。\n
                        """));
        vectorStore.add(documents);


        VectorStoreDocumentRetriever vectorStoreDocumentRetriever
                = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5)       // 相似度阈值
                .topK(3)                        // 返回文档数量
                .filterExpression(new FilterExpressionBuilder()
                        .eq("genre", "fairytale")
                        .build())     // 文档过滤表达式
                .build();
        // 允许空上下文查询
        ContextualQueryAugmenter contextualQueryAugmenter = ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();

        // 创建检索增强顾问
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor =
                RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(vectorStoreDocumentRetriever)
                        .queryAugmenter(contextualQueryAugmenter)
                        .build();

        //  在聊天客户端中使用顾问
        String message = "机器人有什么功能？";
        System.out.println("【提问】: " + message);

        String content = chatClient.prompt(message)
                .advisors(retrievalAugmentationAdvisor)
                .call()
                .content();

        System.out.println("回答 = " + content);

    }


}
