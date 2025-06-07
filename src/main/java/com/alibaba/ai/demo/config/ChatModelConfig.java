package com.alibaba.ai.demo.config;


import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * 通义千问模型
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
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
