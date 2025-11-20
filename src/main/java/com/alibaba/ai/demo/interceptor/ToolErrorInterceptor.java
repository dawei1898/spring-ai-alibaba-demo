package com.alibaba.ai.demo.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 工具错误处理拦截器
 *
 * @author dawei
 */
@Slf4j
public class ToolErrorInterceptor extends ToolInterceptor {

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        try {
            return handler.call(request);
        } catch (Exception e) {
            log.error("Failed to interceptToolCall.", e);
            return ToolCallResponse.of(request.getToolCallId(),
                    request.getToolName(), "Tool failed:" + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "toolErrorInterceptor";
    }

}
