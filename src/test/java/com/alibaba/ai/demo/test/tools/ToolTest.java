package com.alibaba.ai.demo.test.tools;

import com.alibaba.ai.demo.model.WeatherRequest;
import com.alibaba.ai.demo.model.WeatherResponse;
import com.alibaba.ai.demo.tools.MyWeatherService;
import com.alibaba.ai.demo.tools.MyWeatherTools;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tool 工具测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ToolTest {


    @Resource
    private DashScopeChatModel dashScopeChatModel;


    /**
     *
     *  测试调用工具
     */
    @Test
    public void test01 () throws Exception {

        // 编程式定义天气工具
        FunctionToolCallback<WeatherRequest, WeatherResponse> toolCallback =
                FunctionToolCallback
                .builder("currentWeather", new MyWeatherService())
                .description("Get the weather in location")
                .inputType(WeatherRequest.class)
                .build();


        ReactAgent reactAgent = ReactAgent.builder()
                .name("weather-agent")
                .model(dashScopeChatModel)
                .tools(toolCallback)
                .build();

        String message = "深圳今天的天气怎么样？";
        System.out.println("【提问】 = " + message);

        AssistantMessage assistantMessage = reactAgent.call(message);

        System.out.println("【回答】 = " + assistantMessage.getText());

    }

    /**
     *
     *  测试调用工具
     */
    @Test
    public void test02 ()  {

        ChatClient chatClient = ChatClient
                .builder(dashScopeChatModel)
                .defaultToolNames("currentWeather")
                .build();

        String message = "深圳今天的天气怎么样？";
        System.out.println("【提问】 = " + message);

        ChatClient.CallResponseSpec response = chatClient.prompt(message).call();

        System.out.println("【回答】 = " + response.content());

    }

}
