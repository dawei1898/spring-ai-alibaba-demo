package com.alibaba.ai.demo.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.Data;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ReactAgent 配置
 *
 * @author dawei
 */
@Data
@Configuration
public class ReactAgentConfig {

    @Bean
    public ReactAgent reactAgent(ChatModel chatModel) {
        return ReactAgent.builder()
                .name("react-agent-config")
                .model(chatModel)
                .build();
    }


}

