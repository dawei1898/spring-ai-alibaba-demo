package com.alibaba.ai.demo.test.graph;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class GraphTest {

    @Autowired
    private ChatClient chatClient;

    @Test
    public void testGraph() throws Exception {

    }
}
