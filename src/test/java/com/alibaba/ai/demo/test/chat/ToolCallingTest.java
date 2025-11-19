package com.alibaba.ai.demo.test.chat;

import com.alibaba.ai.demo.tools.TimeTools;
import com.alibaba.ai.demo.tools.WeatherTools;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.toolcalling.weather.WeatherService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


/**
 * 工具调用 测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ToolCallingTest {

    public static final String MODEL_QWEN_PLUS = "qwen-plus";


    @Resource
    private ChatClient chatClient;

    @Resource
    private TimeTools timeTools;

    @Resource
    private WeatherTools weatherTools;

    @Resource
    private WeatherService weatherService;


    /**
     * 测试时间工具
     */
    @Test
    public void testTimeTool() {
        // 日志记录
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
                request -> "[chat request]: " + request.prompt(),
                response -> "[chat response]: " + response.getResult().getOutput().getText(),
                0
        );


        String message = "北京时间，今天是几号?现在几点了？";
        System.out.println("【提问】: " + message);

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_PLUS)
                .build();

        String content = chatClient.prompt(message)
                .options(chatOptions)
                .advisors(customLoggerAdvisor)
                .tools(timeTools)
                //.tools(new DateTimeTools())
                .call()
                .content();
        System.out.println("【回答】: " + content);

    }

    /**
     * 测试天气工具
     */
    @Test
    public void testWeatherTool() {
        // 日志记录
        SimpleLoggerAdvisor customLoggerAdvisor = new SimpleLoggerAdvisor(
                request -> "[chat request]: " + request.prompt(),
                response -> "[chat response]: " + response.getResult().getOutput().getText(),
                0
        );


        String message = "请告诉我深圳最近 3 天的天气";
        System.out.println("【提问】: " + message);

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_PLUS)
                .build();

        // 天气工具
        /*FunctionToolCallback<WeatherService.Request, WeatherService.Response> getWeather
                = FunctionToolCallback.builder("getWeather", weatherService)
                .description("Use api.weather to get weather information.")
                .inputType(WeatherService.Request.class)
                .build();*/

        String content = chatClient.prompt(message)
                .options(chatOptions)
                .advisors(customLoggerAdvisor)
                .tools(weatherTools)
                .call()
                .content();

        System.out.println("【回答】: " + content);
    }


}
