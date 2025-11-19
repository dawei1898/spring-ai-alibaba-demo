package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 对话测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ChatTest {

    public static final String MODEL_QWEN = "qwen-max";
    public static final String MODEL_QWQ_32B= "qwq-32b"; // 深度思考
    public static final String MODEL_V3 = "deepseek-v3";
    public static final String MODEL_R1 = "deepseek-r1";  // 深度思考

    @Resource
    private ChatClient chatClient;

    @Resource
    private DashScopeChatModel dashScopeChatModel;


    @Test
    public void testChatModel() throws Exception {
        String message = "请问你是什么版本的?";
        System.out.println("提问: " + message);

        // 同步对话 call()
        String call = dashScopeChatModel.call(message);
        System.out.println("ChatModel call = " + call);

        // 流式对话 stream()
        dashScopeChatModel.stream(message).subscribe
                (r -> System.out.println("ChatModel stream content = " + r));
        TimeUnit.SECONDS.sleep(10);

        dashScopeChatModel.stream(new Prompt(message)).subscribe
                (r -> System.out.println("ChatModel prompt stream response =  " + r));
        TimeUnit.SECONDS.sleep(10);

        Flux<String> stringFlux = dashScopeChatModel.stream(new Prompt(message))
                .flatMapSequential(r -> {
                    return Flux.just(r.getResult().getOutput().getText());
                });
        stringFlux.subscribe(f -> System.out.println("ChatModel prompt stream flatMapSequential = " + f));

        // 等待执行结果
        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void testChatClient1() throws Exception {
        String message = "你是谁?";
        System.out.println("提问: " + message);

        // 同步对话 call()
        String content = chatClient.prompt(message).call().content();
        System.out.println("ChatClient prompt call content = " + content);

    }

    @Test
    public void testChatClient2() throws Exception {
        String message = "请问 1 + 2 = ?";
        System.out.println("提问: " + message);

        // 同步对话 call()
        String content = chatClient.prompt(message).call().content();
        System.out.println("ChatClient prompt call content = " + content);

        ChatResponse chatResponse = chatClient.prompt(message).call().chatResponse();
        System.out.println("ChatClient prompt call  chatResponse = " + chatResponse);

        // 流式对话 stream()
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWQ_32B)
                .withTemperature(0.7)
                .build();
        Prompt prompt = new Prompt(new UserMessage(message));
        /*Flux<String> stringFlux = chatClient.prompt(prompt)
                .options(chatOptions)
                //.system("你是一名java高级开发")
                .stream()
                .content()
                .concatWith(Flux.just("[DONE]")) // 结束添加终止标识
                .onErrorResume(e -> Flux.just("ERROR: " + e.getMessage(), "[DONE]")) // 异常也添加终止标识
                ;
        stringFlux.subscribe(f -> System.out.println
                ("ChatClient model[deepseek-v3] prompt stream content = " + f)
        );*/

        Flux<Map<String, String>> mapFlux = chatClient.prompt(prompt)
                .options(chatOptions) // 选择模型
                .system("你是一名数学专家") // 系统人设
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    System.out.println("ChatClient model[deepseek-v3] prompt stream chatResponse = " + r);

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
                ("ChatClient model[deepseek-v3] prompt stream map = " + f)
        );


        // 等待执行结果
        TimeUnit.SECONDS.sleep(60);

    }

    record ActorFilms(String actor, List<String> movies) {}

    @Test
    public void testReturnEntity() throws Exception {
        String message = "你是谁?";
        System.out.println("提问: " + message);

        // 同步对话 call()
        ActorFilms actorFilms = chatClient.prompt(message)
                .call()
                .entity(ActorFilms.class);
        System.out.println("ChatClient prompt call content = " + JSON.toJSONString(actorFilms));

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
