package com.alibaba.ai.demo.controller;

import com.alibaba.ai.demo.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;


/**
 * 聊天客户端
 *
 * @author dawei
 */
@RestController
public class ChatController {

    @Resource
    private ChatService chatService;


    /**
     * 同步响应聊天
     */
    @GetMapping("/ai/chat")
    public Map<String, String> chat(@RequestParam(value = "message") String message,
                                    @RequestParam(value = "model", required = false) String model) {
        return chatService.chat(message, model);
    }

    /**
     * 流式响应聊天
     */
    @GetMapping(path = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> chatStream(@RequestParam(value = "message") String message,
                                         @RequestParam(value = "model", required = false) String model) {
        return chatService.chatStream(message, model);
    }

}
