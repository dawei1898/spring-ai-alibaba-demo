package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgent;
import com.alibaba.cloud.ai.dashscope.agent.DashScopeAgentOptions;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAgentApi;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

/**
 * 阿里云百练-智能体 调试
 *
 * @author dawei
 */
@SpringBootTest
@ActiveProfiles("local")
public class BailianAgentTest {

    /** 智能体联网搜索App ID */
    @Value("${ai.search-agent-app-id}")
    private String search_agent_app_id;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;


    /**
     * Agent
     */
    @Test
    public void testAgent() throws Exception {
        //String message = "深圳今天的天气怎么样？";
        String message = "深圳今天的新闻？";

        DashScopeAgentApi dashScopeAgentApi = new DashScopeAgentApi(apiKey);
        DashScopeAgent dashScopeAgent = new DashScopeAgent(dashScopeAgentApi);
        DashScopeAgentOptions dashScopeAgentOptions = DashScopeAgentOptions
                .builder().withAppId(search_agent_app_id).build();
        Prompt prompt = new Prompt(message, dashScopeAgentOptions);

        ChatResponse chatResponse = dashScopeAgent.call(prompt);
        AssistantMessage app_output = chatResponse.getResult().getOutput();
        String content = app_output.getText();

        System.out.println("【提问】= " + message);
        System.out.println("【联网搜索智能体回答】= " + content);

    }

    /**
     * Agent, 流式回答
     */
    @Test
    public void testAgentStream() throws Exception {
        //String message = "深圳今天的天气怎么样？";
        String message = "深圳今天的新闻？";
        System.out.println("【提问】= " + message);

        DashScopeAgentApi dashScopeAgentApi = new DashScopeAgentApi(apiKey);
        DashScopeAgent dashScopeAgent = new DashScopeAgent(dashScopeAgentApi);
        DashScopeAgentOptions dashScopeAgentOptions = DashScopeAgentOptions
                .builder()
                .withAppId(search_agent_app_id)
                .withIncrementalOutput(true) // 增量返回
                .build();
        Prompt prompt = new Prompt(message, dashScopeAgentOptions);

        Flux<String> stringFlux = dashScopeAgent.stream(prompt).flatMapSequential(r -> {
            AssistantMessage assistantMessage = r.getResult().getOutput();
            String content = assistantMessage.getText();

            return Flux.just(content);
        });

        StringBuilder sb = new StringBuilder();
        stringFlux.subscribe( t -> {
            System.out.println("【response】= " + t);
            sb.append(t);
        });

        TimeUnit.SECONDS.sleep(15);

        System.out.println("【联网搜索智能体回答】= " + sb.toString());
    }
}
