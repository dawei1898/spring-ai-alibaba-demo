package com.alibaba.ai.demo.test.mcp;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MCP 测试
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class MCPTest {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private List<McpAsyncClient> mcpAsyncClients;

    @Autowired
    private AsyncMcpToolCallbackProvider toolCallbackProvider;





    /**
     * 调用指定的 MCP 工具方法
     */
    @Test
    public void test1() throws Exception {
        McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest(
                "maps_weather", Map.of("city", "深圳")
        );
        McpAsyncClient mcpClient = mcpAsyncClients.get(0);
        Mono<McpSchema.CallToolResult> mono = mcpClient.listTools().flatMap(tools -> {
            for (McpSchema.Tool tool : tools.tools()) {
                System.out.println("tool: " + tool);
            }

            return mcpClient.callTool(callToolRequest);
        });
        mono.subscribe(r ->
                        System.out.println("MCP result = " + r.content())
        );

        // 等待执行结果
        TimeUnit.SECONDS.sleep(20);

    }

    /**
     * 测试 高德地图 MCP
     */
    @Test
    public void test02(){
        String message = "深圳今天天气怎么样?";
        System.out.println("[提问]: " + message);

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        String content = chatClient.prompt(message)
                .toolCallbacks(toolCallbacks)
                .call().content();
        System.out.println("content = " + content);
    }

    /**
     * 测试 文件 MCP
     */
    @Test
    public void test03(){
        String message = "/Users/fu/code/IdeaProjects/spring-ai-alibaba-demo，有什么代码?";
        System.out.println("[提问]: " + message);

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        String content = chatClient.prompt(message)
                .toolCallbacks(toolCallbacks)
                .call().content();
        System.out.println("content = " + content);
    }

    /**
     * 测试 文件 MCP
     */
    @Test
    public void test04(){
        String message = "/Users/fu/Downloads，有什么图片?";
        System.out.println("[提问]: " + message);

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        String content = chatClient.prompt(message)
                .toolCallbacks(toolCallbacks)
                .call().content();
        System.out.println("content = " + content);
    }

    /**
     * 测试 文件 MCP
     */
    @Test
    public void test05(){
        String message = "/Users/fu/code/IdeaProjects/spring-ai-alibaba-demo/src/main/resources/mcp/spring-ai-mcp-overview.txt，将文件中的内容翻译为中文?";
        System.out.println("[提问]: " + message);

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        String content = chatClient.prompt(message)
                .toolCallbacks(toolCallbacks)
                .call().content();
        System.out.println("content = " + content);
    }


    /**
     * 测试 playwright MCP
     */
    @Test
    public void test06(){
        String message = "https://docs.shipany.ai/zh，这个页面有什么内容?";
        System.out.println("[提问]: " + message);

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        String content = chatClient.prompt(message)
                .toolCallbacks(toolCallbacks)
                .call().content();
        System.out.println("content = " + content);
    }


    /**
     * 测试 自定义 MCP
     */
    @Test
    public void test07(){
        String message = "深圳今天天气怎么样?";
        System.out.println("[提问]: " + message);

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        String content = chatClient.prompt(message)
                .toolCallbacks(toolCallbacks)
                .call().content();
        System.out.println("content = " + content);
    }
}
