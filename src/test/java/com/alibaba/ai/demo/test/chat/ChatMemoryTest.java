package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;


/**
 * 基于memory的对话记忆 测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ChatMemoryTest {

    public static final String MODEL_QWEN_PLUS = "qwen-plus";

    @Resource
    private ChatClient chatClient;

    @Resource
    private ChatClient chatClient2;

    @Autowired
    private MysqlChatMemoryRepository mysqlChatMemoryRepository;

    /*public ChatMemoryTest(ChatClient.Builder builder, MysqlChatMemoryRepository mysqlChatMemoryRepository) {
        // 基于 mysql 的记忆存储
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(mysqlChatMemoryRepository)
                .maxMessages(10)
                .build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(messageWindowChatMemory)
                .build();

        this.chatClient2 = builder.defaultAdvisors(messageChatMemoryAdvisor).build();
    }*/



    /**
     * 基于内存的对话记忆
     */
    @Test
    public void test01(){
        // 对话唯一标识
        String chatId = UUID.randomUUID().toString();
        String chatId2 = UUID.randomUUID().toString();
        // 基于内存的对话记忆
        ChatMemory chatMemory  = MessageWindowChatMemory.builder()
                .maxMessages(2)
                .build();


        // 日志
        // 日志记录
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
                request -> "[chat request]: " + request.prompt(),
                response -> "[chat response]: " + response.getResult().getOutput().getText(),
                0
        );

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_PLUS)
                .build();

        // 第一次对话
        String message1 = "用 java 输出 hello";
        System.out.println("【提问1】: " + message1);

        chatMemory.add(chatId, new UserMessage(message1));

        Prompt prompt1 = new Prompt(chatMemory.get(chatId));

        String content1 = chatClient.prompt(prompt1)
                .options(chatOptions)
                .advisors( customLoggerAdvisor)
                .call()
                .content();
        System.out.println("【回答1】: " + content1);

        chatMemory.add(chatId, new AssistantMessage(content1));


        // 第二次对话
        System.out.println("===========================");
        String message2 = "再输出 你好";
        System.out.println("【提问2】: " + message2);

        chatMemory.add(chatId, new UserMessage(message2));

        Prompt prompt2 = new Prompt(chatMemory.get(chatId));

        String content2 = chatClient.prompt(prompt2)
                .options(chatOptions)
                .advisors(customLoggerAdvisor)
                .call()
                .content();
        System.out.println("【回答2】: " + content2);

        chatMemory.add(chatId, new AssistantMessage(content2));

        // 第三次对话
        System.out.println("===========================");
        String message3 = "再输出 很高兴";
        System.out.println("【提问3】: " + message3);

        //chatMemory.add(chatId, new UserMessage(message3));
        //Prompt prompt3 = new Prompt(chatMemory.get(chatId));

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .conversationId(chatId)
                .build();

        String content3 = chatClient.prompt(message3)
                .options(chatOptions)
                .advisors(messageChatMemoryAdvisor, customLoggerAdvisor)
                .call()
                .content();
        System.out.println("【回答3】: " + content3);
    }

    /**
     * 基于 Mysql 存储的对话记忆
     */
    @Test
    public void test02(){
        // 对话唯一标识
        String chatId = UUID.randomUUID().toString();

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_PLUS)
                .build();

        // 第一次对话
        String message1 = "用 java 输出 hello";
        System.out.println("【提问1】: " + message1);

        String content1 = chatClient2.prompt(message1)
                .options(chatOptions)
                .advisors(m -> m.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
        System.out.println("【回答1】: " + content1);

        // 第二次对话
        System.out.println("===========================");
        String message2 = "再输出 你好";
        System.out.println("【提问2】: " + message2);

        String content2 = chatClient2.prompt(message2)
                .options(chatOptions)
                .advisors(m -> m.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
        System.out.println("【回答2】: " + content2);

        // 第三次对话
        System.out.println("===========================");
        String message3 = "再输出 很高兴";
        System.out.println("【提问3】: " + message3);

        String content3 = chatClient2.prompt(message3)
                .options(chatOptions)
                .advisors(m -> m.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
        System.out.println("【回答3】: " + content3);
    }


}
