package com.alibaba.ai.demo.test.chat;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文档读取 测试
 *
 * @author dawei
 */

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
public class DocumentReaderTest {


    @Autowired
    private ChatClient chatClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 文档解析
     */
    @Test
    public void tikaDocumentReaderTest() throws Exception {
        String docUrl = "temp/docs/百炼系列手机产品介绍 (1).docx";
        //String markdownUrl = "https://sca-oss-bucket.oss-cn-shenzhen.aliyuncs.com/sfc/%E7%99%BE%E7%82%BC%E6%89%8B%E6%9C%BA%E4%BA%A7%E5%93%81%E4%BB%8B%E7%BB%8D.md?Expires=1753627371&OSSAccessKeyId=TMP.3Kp4q6JgKzAMzNguDurpNyoKgmfYtEgdtXbgBkTx3pabQXn4ug6f47dkPCG3cRPdemqsPUBRfiiEqxBnyudNSEo65thpox&Signature=5SoCZcK0ibCC7uE5CWTuSHzUrUw%3D";
        String message = "附件是什么内容?";
        System.out.println("【提问】: " + message);

        String text = getText(docUrl, null);
        System.out.println("text = " + text);

        String content = chatClient.prompt()
                .user("附件：" + text)
                .user(message)
                .call()
                .content();
        System.out.println("【回答】: " + content);
    }


    private static String getText(String url, MultipartFile file) {

        if (Objects.nonNull(file)) {

            log.debug("Reading file content form MultipartFile");
            List<Document> documents = new TikaDocumentReader(file.getResource()).get();
            return documents.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n\n"));
        }

        if (StringUtils.hasText(url)) {
            log.debug("Reading file content form url");
            List<Document> documents = new TikaDocumentReader(url).get();
            return documents.stream()
                    .map(Document::getFormattedContent)
                    .collect(Collectors.joining("\n\n"));
        }

        return "";
    }


    /**
     * markdown 文档解析
     */
    @Test
    public void markdownDocumentReaderTest() throws Exception {
        String markdownUrl = "classpath:temp/docs/百炼手机产品介绍.md";
        String markdownText = getMarkdownText(markdownUrl);
        System.out.println("markdownText = " + markdownText);

    }

    private static String getMarkdownText(String url ) {
        // 或者从资源路径创建
        MarkdownDocumentReader reader = new MarkdownDocumentReader(url);
        List<Document> documents = reader.get();
        return documents.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * 解析文档并作为向量存储
     */
    @Test
    public void saveDocumentTest() throws Exception {
        // 解析文档
        String docUrl = "temp/docs/百炼系列手机产品介绍 (1).docx";
        String pdfUrl = "temp/docs/百炼系列手机产品介绍.pdf";
        List<Document> documents = new TikaDocumentReader(docUrl).get();

        // 初始化向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        vectorStore.add(documents);

        // 保存向量存储到文件
        File file = new File("src/main/resources/temp/vector_store/docs.json");
        vectorStore.save(file);
    }
}
