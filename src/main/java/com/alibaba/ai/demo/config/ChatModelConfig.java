package com.alibaba.ai.demo.config;


import com.alibaba.cloud.ai.advisor.DocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * 聊天模型配置
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class ChatModelConfig {

    private String dashscopeApiKey;

    public static final String DASHSCOPE_MODEL = "qwen-max";
    public static final String DEEPSEEK_V3_MODEL = "deepseek-v3";
    public static final String DEEPSEEK_R1_MODEL = "deepseek-r1";

    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.jdbc-url}")
    private String mysqlJdbcUrl;
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.username}")
    private String mysqlUsername;
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.password}")
    private String mysqlPassword;
    @Value("${spring.ai.chat.memory.repository.jdbc.mysql.driver-class-name}")
    private String mysqlDriverClassName;

    /**
     * 通义千问模型
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public ChatClient chatClient2(ChatClient.Builder builder, MysqlChatMemoryRepository mysqlChatMemoryRepository) {
        // 日志记录
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
                request -> "[chat request]: " + request.prompt(),
                response -> "[chat response]: " + response.getResult(),
                0
        );

        // 基于 mysql 的记忆存储
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(mysqlChatMemoryRepository)
                .maxMessages(10)
                .build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(messageWindowChatMemory)
                .build();

        // 添加阿里云百炼知识库
        DashScopeDocumentRetrieverOptions documentRetrieverOptions = DashScopeDocumentRetrieverOptions
                .builder()
                .withIndexName("百炼手机产品介绍")
                .build();
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(dashscopeApiKey).build();
        DocumentRetriever retriever = new DashScopeDocumentRetriever(dashScopeApi, documentRetrieverOptions);
        DocumentRetrievalAdvisor documentRetrievalAdvisor = new DocumentRetrievalAdvisor(retriever);

        return builder.defaultAdvisors(
                customLoggerAdvisor,
                messageChatMemoryAdvisor,
                documentRetrievalAdvisor
        ).build();
    }

    //@Bean
    public MysqlChatMemoryRepository mysqlChatMemoryRepository() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(mysqlDriverClassName);
        dataSource.setUrl(mysqlJdbcUrl);
        dataSource.setUsername(mysqlUsername);
        dataSource.setPassword(mysqlPassword);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }


    /**
     * 通义千问模型
     */
    //@Bean
    /*public ChatClient chatClient() {
        DashScopeApi dashscopeApi = new DashScopeApi(dashscopeApiKey);

        DashScopeChatOptions dashscopeChatOptions = DashScopeChatOptions.builder()
                .withModel(DASHSCOPE_MODEL)
                .withTemperature(0.7)
                .build();

        DashScopeChatModel dashScopeChatModel =
                new DashScopeChatModel(dashscopeApi, dashscopeChatOptions);

        String sysPrompt = """
                你是一个博学的智能聊天助手，请根据用户提问回答。
                请讲中文。
                """;
        return ChatClient.builder(dashScopeChatModel).defaultSystem(sysPrompt).build();
    }*/

    /**
     * 通义千问模型
     */
    //@Bean
    /*public DashScopeChatModel dashScopeChatModel() {
        DashScopeApi dashscopeApi = new DashScopeApi(dashscopeApiKey);
        DashScopeChatOptions dashscopeChatOptions = DashScopeChatOptions.builder()
                .withModel(DASHSCOPE_MODEL)
                .withTemperature(0.7)
                .build();
        return new DashScopeChatModel(dashscopeApi, dashscopeChatOptions);
    }*/



}
