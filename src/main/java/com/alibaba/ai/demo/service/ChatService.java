package com.alibaba.ai.demo.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dawei
 */

@Slf4j
@Service
public class ChatService {

    @Resource
    private ChatClient chatClient;

    //@Resource
    //private OpenAiChatModel openAiChatModel;

    @Resource
    private DashScopeChatModel dashScopeChatModel;

    /**
     * 同步响应聊天
     */
    public Map<String, String> chat(String message, String model) {
        log.info("chat model:{}, message:{}", model, message);
        String content = "";
        if (StringUtils.isEmpty(model)) {
            // 默认模型
            content = dashScopeChatModel.call(message);
        } else {
            // 选择模型
            ChatOptions chatOptions = ChatOptions.builder().model(model).build();
            Prompt prompt = new Prompt(message, chatOptions);
            ChatResponse chatResponse = dashScopeChatModel.call(prompt);
            content = chatResponse.getResult().getOutput().getText();
        }
        log.info("chat content:{}", content);
        return Map.of("content", content);
    }

    /**
     * 流式响应聊天
     */
    public Flux<Map<String, String>> chatStream(String message, String model) {
        log.info("chatStream model:{}, message:{}", model, message);

        Prompt prompt = new Prompt(new UserMessage(message));
        if (StringUtils.isNotBlank(model)) {
            // 选择模型
            ChatOptions chatOptions = ChatOptions.builder().model(model).build();
            prompt = new Prompt(message, chatOptions);
        }
        return dashScopeChatModel.stream(prompt).flatMapSequential
                (r -> {
                    //log.info("response: {}", JSON.toJSONString(r));

                    Map<String, String> map = new HashMap<>();
                    // 思考
                    String reasoningContent = String.valueOf(r.getResult()
                            .getOutput().getMetadata().get("reasoningContent"));
                    if (StringUtils.isNotEmpty(reasoningContent)) {
                        map.put("reasoningContent", reasoningContent);
                    }

                    // 回答
                    String text = r.getResult().getOutput().getText();
                    if (StringUtils.isNotEmpty(text)) {
                        map.put("content", text);
                    }

                    // 结束
                    if (StringUtils.isEmpty(text)
                            && "STOP".equals(r.getResult().getMetadata().getFinishReason())) {
                        map.put("content", "[DONE]");
                    }
                    log.info("map: {}", JSON.toJSONString(map));
                    return Flux.just(map);
                });
    }


}
