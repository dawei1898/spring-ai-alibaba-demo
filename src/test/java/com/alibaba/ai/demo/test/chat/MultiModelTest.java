package com.alibaba.ai.demo.test.chat;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.chat.MessageFormat;
import com.alibaba.cloud.ai.dashscope.common.DashScopeApiConstants;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 多模态输入测试
 *
 * Debug 调试断点：DefaultRestClient#applyStatusHandlers()
 *
 * @author dawei
 */
@SpringBootTest
@ActiveProfiles("local")
public class MultiModelTest {

    // 全模态模型
    public static final String MODEL_QWEN_OMNI_TURBO = "qwen-omni-turbo";
    public static final String MODEL_QWEN25_OMNI_7B = "qwen2.5-omni-7b";

    // 视觉理解模型
    public static final String MODEL_QWEN_VL_PLUS = "qwen-vl-plus";

    // 视觉推理模型
    public static final String MODEL_QVQ_PLUS = "qvq-plus";

    // 文字提取模型
    public static final String MODEL_QWEN_VL_OCR = "qwen-vl-ocr";

    // 音频理解模型
    public static final String MODEL_QWEN_AUDIO_TURBO = "qwen-audio-turbo";

    // 文档处理
    public static final String MODEL_QWEN_LONG = "qwen-long";

    public static final String MODEL_QWEN_MAX = "qwen-max";


    public static final String TEMP_IMAGE_DIR = "temp_file";

    @Autowired
    private ChatClient chatClient;


    /**
     * 测试视觉理解
     */
    @Test
    public void imageReaderTest1() throws Exception {
        String imageUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg";
        String message = "图片描述了什么?";
        System.out.println("【提问】: " + message);

        // 选择模型
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_VL_PLUS)
                .withMultiModel(true) // 开启多模态
                .build();
        // 图片 url
        Media media = new Media(MimeTypeUtils.IMAGE_PNG, new URI(imageUrl).toURL().toURI());
        Map<String, Object> metadata = Map.of(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        //Map<String, Object> metadata = new HashMap<>();
        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .metadata(metadata)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        System.out.println("【回答】 = " + content);
    }

    /**
     * 测试 视觉推理
     * 仅有支持流式回答
     */
    @Test
    public void imageReaderTest2() throws Exception {
        String imageUrl = "https://img.alicdn.com/imgextra/i1/O1CN01gDEY8M1W114Hi3XcN_!!6000000002727-0-tps-1024-406.jpg";
        String message = "这道题怎么解答?";
        System.out.println("【提问】: " + message);

        // 选择模型
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                //.withModel(MODEL_QVQ_PLUS)
                .withModel(MODEL_QWEN_OMNI_TURBO)
                .withMultiModel(true) // 开启多模态
                .build();
        // 图片 url
        Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new URI(imageUrl).toURL().toURI());
        Map<String, Object> metadata = Map.of(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .metadata(metadata)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        Flux<Map<String, String>> mapFlux = chatClient.prompt(prompt)
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    Map<String, String> map = new HashMap<>();
                    if (r.getResult() != null) {
                        // 思考
                        String reasoningContent = String.valueOf(r.getResult()
                                .getOutput().getMetadata().get("reasoningContent"));
                        if (StringUtils.isNotEmpty(reasoningContent)) {
                            map.put("reasoningContent", reasoningContent);
                        }
                        // 回答
                        String text = r.getResult().getOutput().getText();
                        if (StringUtils.isNotEmpty(text)) {
                            map.put("content", text);
                        }
                    }
                    return Flux.just(map);
                });
        mapFlux.subscribe(f -> System.out.println
                ("【回答】: " + f)
        );

        // 等待执行结果
        TimeUnit.SECONDS.sleep(60);
    }


    /**
     * 测试 文字提取
     */
    @Test
    public void imageReaderTest3() throws Exception {
        String imageUrl = "https://img.alicdn.com/imgextra/i2/O1CN01ktT8451iQutqReELT_!!6000000004408-0-tps-689-487.jpg";
        String message = """
                请提取车票图像中的发票号码、车次、起始站、终点站、发车日期和时间点、座位号、席别类型、票价、身份证号码、购票人姓名。
                要求准确无误的提取上述关键信息、不要遗漏和捏造虚假信息，模糊或者强光遮挡的单个文字可以用英文问号?代替。
                返回数据格式以json方式输出，格式为：{'发票号码'：'xxx', '车次'：'xxx', '起始站'：'xxx', '终点站'：'xxx', 
                '发车日期和时间点'：'xxx', '座位号'：'xxx', '席别类型'：'xxx','票价':'xxx', '身份证号码'：'xxx', '购票人姓名'：'xxx'
                """;
        System.out.println("【提问】: " + message);

        // 选择模型
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_VL_OCR)
                .withMultiModel(true) // 开启多模态
                .build();
        // 图片 url
        Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new URI(imageUrl).toURL().toURI());
        Map<String, Object> metadata = Map.of(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .metadata(metadata)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        System.out.println("【回答】 = " + content);
    }


    /**
     * 测试 音频理解 TODO 报错
     */
    @Test
    public void audioReaderTest() throws Exception {
        String message = "这段音频在说什么?";
        System.out.println("【提问】: " + message);

        // 选择模型
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                //.withModel(MODEL_QWEN_AUDIO_TURBO)
                .withModel(MODEL_QWEN_OMNI_TURBO)
                .withMultiModel(true) // 开启多模态
                .build();

        List<Media> medias = new ArrayList<>();
        // 音频 url
        String audioUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3";
        Media media1 = Media.builder()
                .data(new URI(audioUrl))
                .mimeType(MimeType.valueOf("audio/mp3"))
                .build();
        //medias.add(media1);

        // 音频资源路径
        String audioPath = "temp/audio/welcome.mp3";
        Media media2 = Media.builder()
                .data(new ClassPathResource(audioPath))
                .mimeType(MimeTypeUtils.parseMimeType("audio/mp3"))
                .build();
        medias.add(media2);


        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(medias)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        /*ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        System.out.println("【回答】 = " + content);*/

        Flux<Object> mapFlux = chatClient.prompt(prompt)
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    Map<String, String> map = new HashMap<>();
                    if (r.getResult() != null) {
                        // 思考
                        String reasoningContent = String.valueOf(r.getResult()
                                .getOutput().getMetadata().get("reasoningContent"));
                        if (StringUtils.isNotEmpty(reasoningContent)) {
                            map.put("reasoningContent", reasoningContent);
                        }
                        // 回答
                        String text = r.getResult().getOutput().getText();
                        if (StringUtils.isNotEmpty(text)) {
                            map.put("content", text);
                        }
                    }
                    return Flux.just(map);
                });
        mapFlux.subscribe(f -> System.out.println
                ("【回答】: " + f)
        );

        // 等待执行结果
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 测试 视频理解  TODO 报错
     */
    @Test
    public void videoReaderTest1() throws Exception {
        String message = "这段视频的内容是什么?";
        System.out.println("【提问】: " + message);

        // 选择模型
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN25_OMNI_7B)
                .withMultiModel(true) // 开启多模态
                .build();
        // 根据 视频文件URL 生成 file
        Media media = Media.builder()
                .data(new ClassPathResource("temp/video/video1.mp4"))
                .mimeType(Media.Format.VIDEO_MP4)
                .build();
        Map<String, Object> metadata = Map.of(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.VIDEO);
        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .metadata(metadata)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        System.out.println("【回答】 = " + content);

    }

    /**
     * 测试 视频理解
     */
    @Test
    public void videoReaderTest2() throws Exception {
        String videoUrl = "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241115/cqqkru/1.mp4";
        String message = "这段视频的内容是什么?";
        System.out.println("【提问】: " + message);

        // 选择模型
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_VL_PLUS)
                .withMultiModel(true) // 开启多模态
                .build();
        // 根据 视频文件URL 生成 file
        String tempVideoPath = TEMP_IMAGE_DIR + "/" + StringUtils.substringAfterLast(videoUrl, "/");
        File videoFile = downloadFile(videoUrl, tempVideoPath);
        // 3. 从视频中提取10帧
        List<File> frames = extractFrames(videoFile, 10);

        // 4. 准备AI分析所需的媒体列表
        List<Media> mediaList = new ArrayList<>();
        for (File frame : frames) {
            mediaList.add(new Media(
                    MimeTypeUtils.IMAGE_PNG,
                    new FileSystemResource(frame)
            ));
        }

        // 8. 清理临时文件
        cleanUpFiles(frames);
        Files.deleteIfExists(Path.of(tempVideoPath));

        Map<String, Object> metadata = Map.of(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(mediaList)
                .metadata(metadata)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        ChatResponse chatResponse = chatClient.prompt(prompt)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        System.out.println("【回答】 = " + content);


    }

    /**
     * 根据远程 URL 下载文件并返回本地 File 对象
     *
     * @param urlString     远程文件的 URL
     * @param destinationPath 本地保存路径（可以是相对路径或绝对路径）
     * @return 生成的本地 File 对象
     * @throws Exception 网络或IO异常
     */
    private  File downloadFile(String urlString, String destinationPath) throws Exception {
        URL url = new URL(urlString);
        Path destination = Paths.get(destinationPath);

        try (InputStream in = url.openStream()) {
            // 创建父目录（如果不存在）
            Files.createDirectories(destination.getParent());

            // 下载并保存文件
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        // 返回 java.io.File 对象
        return destination.toFile();
    }

    /**
     * 从视频中提取指定数量的帧
     * @param videoFile 视频文件
     * @param frameCount 要提取的帧数
     * @return 提取的帧图片文件列表
     */
    private List<File> extractFrames(File videoFile, int frameCount) throws IOException {
        List<File> frames = new ArrayList<>();
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile);
        grabber.start();

        try {
            // 创建临时目录（如果不存在）
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_IMAGE_DIR);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            int totalFrames = grabber.getLengthInFrames();
            int step = totalFrames / frameCount; // 计算帧间隔

            Java2DFrameConverter converter = new Java2DFrameConverter();
            for (int i = 0; i < frameCount; i++) {
                int frameNumber = i * step; // 计算当前帧位置
                grabber.setFrameNumber(frameNumber);
                Frame frame = grabber.grabImage();
                BufferedImage image = converter.convert(frame);

                // 保存帧为PNG图片
                File outputFile = tempDir.resolve(UUID.randomUUID() + ".png").toFile();
                ImageIO.write(image, "png", outputFile);
                frames.add(outputFile);
            }
        } finally {
            grabber.stop();
        }

        return frames;
    }

    /**
     * 清理临时文件
     * @param files 要删除的文件列表
     */
    private void cleanUpFiles(List<File> files) {
        for (File file : files) {
            file.delete();
        }
    }


    /**
     * 文档读取 TODO 报错
     */
    @Test
    public void docReaderTest1() throws Exception {
        String mdUrl = "https://sca-oss-bucket.oss-cn-shenzhen.aliyuncs.com/sfc/%E7%99%BE%E7%82%BC%E6%89%8B%E6%9C%BA%E4%BA%A7%E5%93%81%E4%BB%8B%E7%BB%8D.md?Expires=1754156070&OSSAccessKeyId=TMP.3KsnRpT8pdGBH8LdZ2ywaJVLqHKgbFgwEDxMtDAaf9BFdA2TwW1cqXMVkQrNSrmKk2fwdtgmeSTZ7Z4VyYrsvQ9z8BxWm8&Signature=qfHvbVBRC6dmGxe5DcgJiiC73AI%3D";
        String txtUrl = "temp/docs/百炼手机产品介绍.txt";
        String message = "文档是什么内容?";
        System.out.println("【提问】: " + message);

        // 选择模型
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                //.withModel("qvq-plus")
                .withModel(MODEL_QWEN25_OMNI_7B)
                .withMultiModel(true) // 开启多模态
                .build();
        // 文档
        Media media = new Media(Media.Format.DOC_MD, new URI(mdUrl).toURL().toURI());
        //Media media = new Media(Media.Format.DOC_TXT, new ClassPathResource(txtUrl));
        Map<String, Object> metadata = Map.of(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);
        // 提问信息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(media)
                .metadata(metadata)
                .build();
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(userMessage, chatOptions);

        Flux<Object> mapFlux = chatClient.prompt(prompt)
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    Map<String, String> map = new HashMap<>();
                    if (r.getResult() != null) {
                        // 思考
                        String reasoningContent = String.valueOf(r.getResult()
                                .getOutput().getMetadata().get("reasoningContent"));
                        if (StringUtils.isNotEmpty(reasoningContent)) {
                            map.put("reasoningContent", reasoningContent);
                        }
                        // 回答
                        String text = r.getResult().getOutput().getText();
                        if (StringUtils.isNotEmpty(text)) {
                            map.put("content", text);
                        }
                    }
                    return Flux.just(map);
                });
        mapFlux.subscribe(f -> System.out.println
                ("【回答】: " + f)
        );

        // 等待执行结果
        TimeUnit.SECONDS.sleep(60);
    }

    /**
     * 长文档处理
     * 1. 通过文件ID传入文档信息
     * 2. 通过纯文传入文档信息
     */
    @Test
    public void docReaderTest2() throws Exception {
        // 上传到阿里云百炼的文件ID
        String  fileId = "file-fe-0e8300d5c3054c0baf45f968";
        // 通过文件ID传入文档信息
        SystemMessage systemMessage = new SystemMessage("fileid://" + fileId);

        // 文档路径
        String docUrl = "temp/docs/产品经理智能体提示词模板.md";
        // 通过纯文本传入信息
        List<Document> documents = new TikaDocumentReader(docUrl).get();
        // 文本信息
        SystemMessage systemMessage2 = new SystemMessage("文档内容：" + documents.get(0).getText());

        String message = "总结此文档";
        System.out.println("【提问】: " + message);

        // 选择模型
        ChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(MODEL_QWEN_LONG)
                .build();

        // 提问信息
        UserMessage userMessage = new UserMessage( message);
        // 构建完整的 Prompt 对象
        Prompt prompt = new Prompt(List.of(systemMessage2,userMessage), chatOptions);

        Flux<Object> mapFlux = chatClient.prompt(prompt)
                .stream()
                .chatResponse()
                .flatMapSequential(r -> {
                    Map<String, String> map = new HashMap<>();
                    if (r.getResult() != null) {
                        // 思考
                        String reasoningContent = String.valueOf(r.getResult()
                                .getOutput().getMetadata().get("reasoningContent"));
                        if (StringUtils.isNotEmpty(reasoningContent)) {
                            map.put("reasoningContent", reasoningContent);
                        }
                        // 回答
                        String text = r.getResult().getOutput().getText();
                        if (StringUtils.isNotEmpty(text)) {
                            map.put("content", text);
                        }
                    }
                    return Flux.just(map);
                });
        mapFlux.subscribe(f -> System.out.println
                ("【回答】: " + f)
        );

        // 等待执行结果
        TimeUnit.SECONDS.sleep(60);
    }
}
