package com.alibaba.ai.demo.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.customizer.McpAsyncClientCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP 自定义设置
 *
 * @author dawei
 */
//@Component
public class CustomMcpAsyncClientCustomizer implements McpAsyncClientCustomizer {

    @Override
    public void customize(String name, McpClient.AsyncSpec spec) {

        spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
            // Handle prompts change
            for (McpSchema.Prompt prompt : prompts) {
                System.out.println("Mcp prompt change = " + prompt);
            }
            return null;
        });

        // Adds a consumer to be notified when logging messages are received from the server.
        spec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {
            // Handle log messages

            System.out.println("Mcp log.data() = " + log.data());
            return null;
        });
    }
}
