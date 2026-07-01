package com.alibaba.ai.demo.tools;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 对话总结工具
 *
 * @author dawei
 */
public class ConversationSummaryTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(String input, ToolContext toolContext) {
        // 状态
        OverAllState state = (OverAllState)toolContext.getContext().get("state");
        // 运行配置
        RunnableConfig config = (RunnableConfig)toolContext.getContext().get("config");
        // 拓展状态
        Map<String, Object> extraState = (Map<String, Object>)toolContext.getContext().get("extraState");

        // 从state中获取消息
        List<Message> messages = (List<Message>) state.value("messages").orElse(null);

        if (messages == null) {
            return "No conversation history available";
        }

        long userMsgs = messages.stream()
                .filter(m -> m.getMessageType().getValue().equals("user"))
                .count();
        long aiMsgs = messages.stream()
                .filter(m -> m.getMessageType().getValue().equals("assistant"))
                .count();
        long toolMsgs = messages.stream()
                .filter(m -> m.getMessageType().getValue().equals("tool"))
                .count();

        return String.format(
                "Conversation has %d user messages, %d AI responses, and %d tool results",
                userMsgs, aiMsgs, toolMsgs
        );
    }

}
