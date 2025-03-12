package com.alibaba.ai.demo.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
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
    public ChatClient chatClient() {
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
    }

    /**
     * 通义千问模型
     */
    @Bean
    public DashScopeChatModel dashScopeChatModel() {
        DashScopeApi dashscopeApi = new DashScopeApi(dashscopeApiKey);
        DashScopeChatOptions dashscopeChatOptions = DashScopeChatOptions.builder()
                .withModel(DASHSCOPE_MODEL)
                .withTemperature(0.7)
                .build();
        return new DashScopeChatModel(dashscopeApi, dashscopeChatOptions);
    }


    /**
     * DeepSeek 聊天客户端
     */
    /*@Bean(name = "deepSeekChatModel")
    public OpenAiChatModel deepSeekChatModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(DEEPSEEK_BASE_URL)
                .apiKey(DEEPSEEK_API_KEY)
                .build();

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .model(DEEPSEEK_MODEL)
                .temperature(0.7)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(openAiChatOptions)
                .build();
    }*/

}
