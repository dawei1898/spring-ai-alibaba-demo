package com.alibaba.ai.demo.test.agents;

import com.alibaba.ai.demo.interceptor.ToolErrorInterceptor;
import com.alibaba.ai.demo.tools.SearchTool;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ReactAgent 测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ReactAgentTest {

    public static final String MODEL_NAME = "qwen-plus";

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Resource
    private DashScopeChatModel chatModel;

    @Resource
    private ReactAgent reactAgent;

    /**
     * 测试配置模型
     */
    @Test
    public void testChatModel() throws Exception {

        DashScopeApi dashScopeApi = DashScopeApi
                .builder().apiKey(dashscopeApiKey).build();
        DashScopeChatOptions defaultOptions = DashScopeChatOptions
                .builder().withModel(MODEL_NAME).build();
        DashScopeChatModel dashScopeChatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(defaultOptions)
                .build();

        ReactAgent reactAgent = ReactAgent.builder()
                .name("react-agent-01")
                .model(dashScopeChatModel)
                .build();

        String message = "请用中文回答，你叫什么名字？";
        System.out.println("【提问】 = " + message);

        AssistantMessage resp = reactAgent.call(message);
        String text = resp.getText();
        System.out.println("【回答】 = " + text);
    }


    /**
     * 测试 ReactAgent 同步对话
     */
    @Test
    public void testReactAgent01() throws Exception {
        String message = "请用中文回答，你叫什么名字？";
        System.out.println("【提问】 = " + message);

        ReactAgent reactAgent = ReactAgent.builder()
                .name("react-agent-01")
                .model(chatModel)
                .build();

        AssistantMessage resp = reactAgent.call(message);
        String text = resp.getText();
        System.out.println("【回答】 = " + text);

    }


    /**
     * 测试 ReactAgent 流式对话
     */
    @Test
    public void testReactAgentStream01() throws Exception {
        String message = "请用中文回答，你叫什么名字？";
        System.out.println("【提问】 = " + message);

        /*ReactAgent reactAgent = ReactAgent.builder()
                .name("react-agent-01")
                .model(chatModel)
                .build();*/

        Flux<NodeOutput> flux = reactAgent.stream(message);

        /*flux.subscribe(
                response -> System.out.println("进度: " + response),
                error -> System.err.println("错误: " + error),
                () -> System.out.println("完成")
        );*/

        flux.subscribe(nodeOutput -> {
            System.out.println("【response】 = " + nodeOutput);
            if (nodeOutput instanceof StreamingOutput streamingOutput) {
                System.out.println("【回答中】 = " + streamingOutput.message().getText());
            }
            if ("__END__".equalsIgnoreCase(nodeOutput.node())) {
                try {
                    Map<String, Object> data = nodeOutput.state().data();
                    List<AssistantMessage> messages = (List<AssistantMessage>)data.get("messages");
                    for (Message mes : messages) {
                        if (mes instanceof  AssistantMessage assistantMessage) {
                            System.out.println("【回答完成】 = " + assistantMessage.getText());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        TimeUnit.SECONDS.sleep(10);
    }



    /**
     * 测试 ReactAgent 调用工具
     */
    @Test
    public void testTools01() throws Exception {
        ToolCallback searchTool = FunctionToolCallback
                .builder("search", new SearchTool())
                .description("搜索工具")
                .inputType(Map.class)
                .build();

        ReactAgent reactAgent = ReactAgent.builder()
                .name("search-agent")
                .model(chatModel)
                .systemPrompt("你是一个专业的技术助手。请准确、简洁地回答问题。")
                .tools(searchTool)
                .interceptors(new ToolErrorInterceptor())
                .build();

        String message = "查询深圳天气并推荐活动";
        //String message = "深圳今天的天气怎么样？";
        System.out.println("【提问】 = " + message);

        AssistantMessage assistantMessage = reactAgent.call(message);
        String content = assistantMessage.getText();
        System.out.println("【回答】 = " + content);
    }

}
