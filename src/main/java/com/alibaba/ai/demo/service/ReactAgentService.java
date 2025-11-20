package com.alibaba.ai.demo.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReactAgent 服务
 *
 * @author dawei
 */

@Slf4j
@Service
public class ReactAgentService {

    public static final String AGENT_NAME = "react-agent-1";

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;


    @Resource
    private DashScopeChatModel dashScopeChatModel;

    /**
     * 同步响应聊天
     */
    public Map<String, String> chat(String message, String model) {
        log.info("chat model:{}, message:{}", model, message);
        try {
            ChatModel chatModel = dashScopeChatModel;
            if (StringUtils.isNotEmpty(model)) {
                // 选择模型
                chatModel = DashScopeChatModel.builder()
                        .dashScopeApi(
                                DashScopeApi.builder().apiKey(dashscopeApiKey).build()
                        )
                        .defaultOptions(
                                DashScopeChatOptions.builder().withModel(model).build()
                        ).build();
            }

            ReactAgent reactAgent = ReactAgent.builder()
                    .name(AGENT_NAME)
                    .model(chatModel)
                    .build();
            AssistantMessage assistantMessage = reactAgent.call(message);
            String content = assistantMessage.getText();

            log.info("chat content:{}", content);
            return Map.of("content", content);
        } catch (GraphRunnerException e) {
            log.error("chat error:", e);
            return Map.of("content", e.getMessage());
        }
    }

    /**
     * 流式响应聊天
     */
    public Flux<Map<String, String>> chatStream(String message, String model) {
        log.info("chatStream model:{}, message:{}", model, message);

        Flux<NodeOutput> flux = null;
        try {
            ChatModel chatModel = dashScopeChatModel;
            if (StringUtils.isNotEmpty(model)) {
                // 选择模型
                chatModel = DashScopeChatModel.builder()
                        .dashScopeApi(
                                DashScopeApi.builder().apiKey(dashscopeApiKey).build()
                        )
                        .defaultOptions(
                                DashScopeChatOptions.builder().withModel(model).build()
                        ).build();
            }

            ReactAgent reactAgent = ReactAgent.builder()
                    .name(AGENT_NAME)
                    .model(chatModel)
                    .build();
            flux = reactAgent.stream(new UserMessage(message));
        } catch (GraphRunnerException e) {
            log.error("chatStream error:", e);
            return Flux.just(Map.of("content", e.getMessage()));
        }

        return flux.flatMapSequential
                        (nodeOutput -> {
                            //log.info("response: {}", nodeOutput);
                            Map<String, String> map = new HashMap<>();
                            // 回答中
                            if (nodeOutput instanceof StreamingOutput streamingOutput) {
                                log.info("【回答中】 = " + streamingOutput.message().getText());
                                // 思考
                                Object reasoningContentObj = streamingOutput.message().getMetadata().get("reasoningContent");
                                if (reasoningContentObj instanceof String reasoningContent) {
                                    if (StringUtils.isNotEmpty(reasoningContent)) {
                                        map.put("reasoningContent", reasoningContent);
                                    }
                                }
                                // 回答
                                String text = streamingOutput.message().getText();
                                if (StringUtils.isNotEmpty(text)) {
                                    map.put("content", text);
                                }
                            }
                            // 回答结束
                            if ("__END__".equalsIgnoreCase(nodeOutput.node())) {
                                try {
                                    Map<String, Object> data = nodeOutput.state().data();
                                    List<AssistantMessage> messages = (List<AssistantMessage>) data.get("messages");
                                    for (Message mes : messages) {
                                        if (mes instanceof AssistantMessage assistantMessage) {
                                            log.info("【回答完成】 = " + assistantMessage.getText());
                                            map.put("content", "[DONE]");
                                        }
                                    }
                                } catch (Exception e) {
                                    log.error("chatStream error:", e);
                                }
                            }
                            log.info("map: {}", JSON.toJSONString(map));
                            return Flux.just(map);
                        })
                // 增加结束标识
                //.concatWith(Flux.just(Map.of("content", "[DONE]")))
                // 异常处理后, 也增加结束标识
                .onErrorResume(e -> {
                    log.error("chatStream error:", e);
                    return Flux.just(Map.of("content", "[DONE]"));
                });
    }

}
