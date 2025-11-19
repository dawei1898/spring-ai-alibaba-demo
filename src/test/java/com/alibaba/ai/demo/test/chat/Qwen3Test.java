package com.alibaba.ai.demo.test.chat;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Qwen3 模型测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class Qwen3Test {

    // Qwen3 商业版
    public static final String MODEL_QWEN_PLUS_0428 = "qwen-plus-2025-04-28";

    // Qwen3 开源版
    public static final String MODEL_QWEN3_32B = "qwen3-32b";

    // Qwen 推理模型
    public static final String MODEL_QWQ_32B = "qwq-32b";


    @Resource
    private ChatClient chatClient;



    /**
     * 测试联网搜索
     */
    @Test
    public void testSearch() throws Exception {
        String message = "深圳今天有什么新闻?";
        System.out.println("【提问】: " + message);

        // 设置
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_PLUS_0428)
                .withEnableSearch(true) // 开启联网搜索
                .build();

        // 流式对话 stream()
        StringBuilder sb = new StringBuilder();
        Flux<String> stringFlux = chatClient.prompt(new Prompt(message))
                .options(chatOptions)
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    return Flux.just(r.getResult().getOutput().getText());
                });
        stringFlux.subscribe(f -> {
            System.out.println("response = " + f);
            sb.append(f);
        });

        // 等待执行结果
        TimeUnit.SECONDS.sleep(10);

        System.out.println("【联网搜索回答】= " + sb);
    }


    /**
     * 思考模式
     */
    @Test
    public void testChatClient2() throws Exception {
        String message = "请问 1 + 2 = ?";
        System.out.println("提问: " + message);

        // 流式对话 stream()
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                //.withModel(MODEL_QWQ_32B) // 推理模型
                .withModel(MODEL_QWEN_PLUS_0428)
                .build();

        Prompt prompt = new Prompt(new UserMessage(message));

        Flux<Map<String, String>> mapFlux = chatClient.prompt(prompt)
                .options(chatOptions) // 选择模型
                .system("你是一名数学专家") // 系统人设
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    System.out.println("ChatClient  stream chatResponse = " + r);

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

                    return Flux.just(map);
                })
                // 增加结束标识
                .concatWith(Flux.just(Map.of("content", "[DONE]")))
                // 异常处理后, 也增加结束标识
                .onErrorResume(e -> {
                    System.out.println("stream chat  error:" + e);
                    return Flux.just(Map.of("content", "[DONE]"));
                });
        mapFlux.subscribe(f -> System.out.println
                ("ChatClient prompt stream map = " + f)
        );

        // 等待执行结果
        TimeUnit.SECONDS.sleep(30);

    }


}
