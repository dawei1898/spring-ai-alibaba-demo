package com.alibaba.ai.demo.test.chat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ChatClient 测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ChatClientTest {

    public static final String MODEL_QWEN = "qwen-max";
    public static final String MODEL_QWQ_32B= "qwq-32b"; // 深度思考


    @Autowired
    private ChatClient chatClient;


    /**
     * 同步对话 call()
     */
    @Test
    public void testChatClient1() throws Exception {
        String message = "你是谁?";
        System.out.println("[提问]: " + message);

        String content = chatClient.prompt().user(message).call().content();
        System.out.println("ChatClient user call content = " + content);

        content = chatClient.prompt(message).call().content();
        System.out.println("ChatClient prompt call content = " + content);

        content = chatClient.prompt(new Prompt(message)).call().content();
        System.out.println("ChatClient new prompt call content = " + content);
    }

    record ActorFilms(String actor, List<String> movies) {

    }
    /**
     * 返参处理
     */
    @Test
    public void testChatResponse() throws Exception {
        String message = "你是谁?";
        System.out.println("[提问]: " + message);

        // 返回 ChatResponse
        ChatResponse chatResponse = chatClient.prompt(message).call().chatResponse();
        System.out.println("ChatClient call chatResponse = " + chatResponse);

        // 返回实体类（Entity）
        ActorFilms actorFilms = chatClient.prompt(message)
                .call()
                .entity(ActorFilms.class);
        System.out.println("ChatClient call entity = " + actorFilms);

        List<ActorFilms> actorFilmsList= chatClient.prompt(message)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});
        System.out.println("ChatClient call list entity = " + actorFilmsList);

    }


    /**
     * 设置系统提示词
     */
    @Test
    public void testSystem() throws Exception {
        String message = "你是谁? 今天是几号几点? ";
        System.out.println("[提问]: " + message);

        String sysPrompt = """
                你是一个博学的智能聊天助手，请根据用户提问回答。
                请讲中文。
                今天日期是{current_time}。
                """;

        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("current_time = " + formattedDateTime);

        String content = chatClient.prompt()
                .system(sysPrompt) // 设置系统提示词
                .system(s -> s.param("current_time", formattedDateTime)) // 替换参数
                .user(message)
                .call()
                .content();
        System.out.println("ChatClient call content = " + content);
    }


    /**
     * Advisors
     */
    @Test
    public void testChatAdvisor() throws Exception {
        String message = "你是谁?";
        System.out.println("提问: " + message);

        // 日志记录
        SimpleLoggerAdvisor simpleLoggerAdvisor = new SimpleLoggerAdvisor();
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
                request -> "chat request: " + request.prompt(),
                response -> "chat response: " + response.getResult(),
                0
        );

        // 同步对话 call()
        String content2 = chatClient.prompt(message)
                .advisors(
                        customLoggerAdvisor
                )
                .call()
                .content();
        System.out.println("ChatClient prompt call content = " + content2);
    }


}
