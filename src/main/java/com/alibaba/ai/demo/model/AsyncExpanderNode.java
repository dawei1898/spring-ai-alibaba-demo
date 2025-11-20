package com.alibaba.ai.demo.model;


import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.async.AsyncGenerator;
import com.alibaba.cloud.ai.graph.async.internal.reactive.GeneratorSubscriber;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AsyncExpanderNode implements NodeAction {

    private static final PromptTemplate DEFAULTPROMPTTEMPLATE = new PromptTemplate("""
            You are an expert at information retrieval and search optimization.
            Your task is to generate {number} different versions of the given query.
            Each variant must cover different perspectives or aspects of the topic,
            while maintaining the core intent of the original query. The goal is to
            expand the search space and improve the chances of finding relevant information.
            Do not explain your choices or add any other text.
            Provide the query variants separated by newlines.
            Original query: {query}
            Query variants:
            """);

    private final ChatClient chatClient;

    private final Integer NUMBER = 3;

    public AsyncExpanderNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String query = state.value("query", "");
        Integer expanderNumber = state.value("expandernumber", this.NUMBER);

        Flux<ChatResponse> chatResponseFlux = this.chatClient.prompt().user(
                (user) -> user.text(DEFAULTPROMPTTEMPLATE.getTemplate())
                        .param("number", expanderNumber)
                        .param("query", query))
                .stream()
                .chatResponse();

        /*AsyncGenerator<? extends NodeOutput> generator = GeneratorSubscriber.builder()
                .startingNode("expanderllmstream")
                .startingState(state)
                .mapResult(response -> {
                    String text = response.getResult().getOutput().getText();
                    List<String> queryVariants = Arrays.asList(text.split("\n"));
                    return Map.of("expandercontent", queryVariants);
                }).build(chatResponseFlux);*/
        AsyncGenerator<? extends NodeOutput> generator = null;
        return Map.of("expandercontent", generator);
    }
}