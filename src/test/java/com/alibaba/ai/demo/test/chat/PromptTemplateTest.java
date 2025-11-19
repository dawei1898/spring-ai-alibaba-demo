package com.alibaba.ai.demo.test.chat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

/**
 * 提示词
 *
 * @author dawei
 */
@SpringBootTest
@ActiveProfiles("local")
public class PromptTemplateTest {


    public static final String MODEL_QWEN_PLUS = "qwen-plus";


    @Autowired
    private ChatClient chatClient;

    /**
     * 提示词模板
     */
    @Test
    public void testChatTemplate() throws Exception {
        String message = "请问 11 + 22 = 13 对吗";

        PromptTemplate promptTemplate = new PromptTemplate("请从专业人士的角度,帮我解决一下问题:{message}");
        Prompt prompt = promptTemplate.create(Map.of("message", message));

        ChatOptions chatOptions = ChatOptions.builder().model(MODEL_QWEN_PLUS).build();

        String content = chatClient.prompt(prompt)
                .options(chatOptions)
                .call()
                .content();
        System.out.println("ChatClient promptTemplate call content = " + content);
    }
}
