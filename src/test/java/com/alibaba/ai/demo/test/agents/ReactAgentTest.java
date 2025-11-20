package com.alibaba.ai.demo.test.agents;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
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

    @Resource
    private DashScopeChatModel chatModel;

    @Resource
    private ReactAgent reactAgent;


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




}
