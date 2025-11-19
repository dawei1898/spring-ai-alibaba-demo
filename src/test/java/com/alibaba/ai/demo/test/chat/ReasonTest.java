package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 推理模型测试
 *
 * @author dawei
 */

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
public class ReasonTest {

    // 深度思考模型
    public static final String MODEL_QWQ_32B = "qwq-32b";


    @Resource
    private ChatClient chatClient;

    @Resource
    private DashScopeChatModel dashScopeChatModel;


    /**
     * 简单对话
     */
    @Test
    public void testChat() throws Exception {
        System.out.println("========== 简单对话 ==========");
        String message = "9.9 和 9.11 谁大 ?";
        System.out.println("[提问]: " + message);

        String model = MODEL_QWQ_32B;
        System.out.println("[模型]: " + model);

        // 流式对话 stream()
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(model)
                .build();
        Prompt prompt = new Prompt(message, chatOptions);

        dashScopeChatModel.stream(prompt)
                .subscribe(r -> {
                    System.out.println("ChatModel model[qwq-32b] prompt stream chatResponse = " + r);
                    // 思考
                    String reasoningContent = String.valueOf(r.getResult()
                            .getOutput().getMetadata().get("reasoningContent"));
                    if (StringUtils.isNotEmpty(reasoningContent)) {
                        System.out.println("[思考] = " + reasoningContent);
                    }
                    // 回答
                    String text = r.getResult().getOutput().getText();
                    if (StringUtils.isNotEmpty(text)) {
                        System.out.println("[回答] = " + text);
                    }
                    // 结束
                    if ("STOP".equals(r.getResult().getOutput().getMetadata().get("finishReason"))) {
                        System.out.println("[结束]");
                    }
                });
        // 等待执行结果
        TimeUnit.SECONDS.sleep(30);
    }


    /**
     * 多轮对话
     */
    @Test
    public void testMultiChat() throws Exception {
        System.out.println("========== 多轮对话 ==========");
        String model = MODEL_QWQ_32B;
        System.out.println("[模型]: " + model);

        Message message1 = new UserMessage("你好");
        Message message2 = new AssistantMessage("你好！很高兴见到你，有什么我可以帮忙的吗？");
        Message message3 = new UserMessage("你是谁");
        List<Message> messageList = Arrays.asList(message1, message2, message3);
        System.out.println("[提问]: " + JSON.toJSONString(messageList));

        // 流式对话 stream()
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(model)
                .withEnableSearch(true)
                .build();
        Prompt prompt = new Prompt(messageList, chatOptions);

        dashScopeChatModel.stream(prompt)
                .subscribe(r -> {
                    System.out.println("ChatModel model[qwq-32b] prompt stream chatResponse = " + r);
                    // 思考
                    String reasoningContent = String.valueOf(r.getResult()
                            .getOutput().getMetadata().get("reasoningContent"));
                    if (StringUtils.isNotEmpty(reasoningContent)) {
                        System.out.println("[思考] = " + reasoningContent);
                    }
                    // 回答
                    String text = r.getResult().getOutput().getText();
                    if (StringUtils.isNotEmpty(text)) {
                        System.out.println("[回答] = " + text);
                    }
                    // 结束
                    if ("STOP".equals(r.getResult().getOutput().getMetadata().get("finishReason"))) {
                        System.out.println("[结束]");
                    }
                });

        // 等待执行结果
        TimeUnit.SECONDS.sleep(30);
    }

    /**
     * 联网搜索
     */
    @Test
    public void testSearch() throws Exception {
        System.out.println("========== 联网搜索 ==========");
        String message = "哪吒2的票房";
        System.out.println("[提问]: " + message);

        String model = MODEL_QWQ_32B;
        System.out.println("[模型]: " + model);

        // 流式对话 stream()
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(model)
                .withEnableSearch(true) // 开启联网搜索
                .build();
        Prompt prompt = new Prompt(message, chatOptions);

        dashScopeChatModel.stream(prompt)
                .subscribe(r -> {
                    System.out.println("ChatModel model[qwq-32b] prompt stream chatResponse = " + r);
                    // 思考
                    String reasoningContent = String.valueOf(r.getResult()
                            .getOutput().getMetadata().get("reasoningContent"));
                    if (StringUtils.isNotEmpty(reasoningContent)) {
                        System.out.println("[思考] = " + reasoningContent);
                    }
                    // 回答
                    String text = r.getResult().getOutput().getText();
                    if (StringUtils.isNotEmpty(text)) {
                        System.out.println("[回答] = " + text);
                    }
                    // 结束
                    if ("STOP".equals(r.getResult().getOutput().getMetadata().get("finishReason"))) {
                        System.out.println("[结束]");
                    }
                });
        // 等待执行结果
        TimeUnit.SECONDS.sleep(30);
    }


    /**
     * 工具调用
     */
    @Test
    public void testFunctionCall() throws Exception {
        System.out.println("========== 工具调用 ==========");
        String message = "深圳今天的天气怎么样?";
        System.out.println("[提问]: " + message);

        String model = MODEL_QWQ_32B;
        System.out.println("[模型]: " + model);

        // 函数工具
        // 获取天气函数
        String get_current_weather = "get_current_weather";
        // 获取日期函数
        String get_current_time = "get_current_weather";
        Set<String> functions = new HashSet<>();
        functions.add("get_current_weather");
        functions.add("get_current_time");

        // 获取天气函数
        ToolDefinition weatherTool = ToolDefinition.builder()
                .name("get_current_weather")
                .description("获取指定地区的天气")
                .inputSchema("{ 'type': 'object', 'properties': { 'location': { 'type': 'string', 'description':'城市或县区，比如北京市、杭州市、余杭区等。' } } }")
                .build();
        // 获取日期函数
        ToolDefinition timeTool = ToolDefinition.builder()
                .name("get_current_time")
                .description("获取当前时刻的时间")
                .inputSchema("{}") // 无参数定义
                .build();


        // 创建回调列表
        List<FunctionToolCallback> functionCallbacks = new ArrayList<>();



        // 流式对话 stream()

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(model)
                //.withToolCallbacks(functions)
                //.withFunctionCallbacks(functionCallbacks)
                .build();
        Prompt prompt = new Prompt(message, chatOptions);



        dashScopeChatModel.stream(prompt)
                .subscribe(r -> {
                    System.out.println("ChatModel model[qwq-32b] prompt stream chatResponse = " + r);
                    // 思考
                    String reasoningContent = String.valueOf(r.getResult()
                            .getOutput().getMetadata().get("reasoningContent"));
                    if (StringUtils.isNotEmpty(reasoningContent)) {
                        System.out.println("[思考] = " + reasoningContent);
                    }
                    // 回答
                    String text = r.getResult().getOutput().getText();
                    if (StringUtils.isNotEmpty(text)) {
                        System.out.println("[回答] = " + text);
                    }
                    // 结束
                    if ("STOP".equals(r.getResult().getOutput().getMetadata().get("finishReason"))) {
                        System.out.println("[结束]");
                    }
                });
        // 等待执行结果
        TimeUnit.SECONDS.sleep(30);
    }


    static class TimeTool {
        public String call() {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return "当前时间：" + now.format(formatter) + "。";
        }
    }

    static class WeatherTool {
        private String location;

        public WeatherTool(String location) {
            this.location = location;
        }

        public String call() {
            return location + "今天是晴天";
        }
    }


}
