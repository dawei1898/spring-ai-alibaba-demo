package com.alibaba.ai.demo.test.audio;


import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisModel;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisResponse;
import com.alibaba.cloud.ai.dashscope.audio.transcription.AudioTranscriptionModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * 测试 语音合成
 *
 * @author dawei
 */
@SpringBootTest
@ActiveProfiles("local")
public class AudioModelTest {


    // 生成语音模型
    public static final String MODEL_COSYVOICE_V2 = "cosyvoice-v2";
    public static final String MODEL_SAMBERT_ZHICHU_V1 = "sambert-zhichu-v1";

    private static final String FILE_PATH = "src/main/resources/temp/audio";

    // 语音翻译模型
    public static final String MODEL_SENSEVOICE_V1 = "sensevoice-v1";


    @Autowired
    private SpeechSynthesisModel speechSynthesisModel;

    @Autowired
    private AudioTranscriptionModel audioTranscriptionModel;


    /**
     * 文生语音
     * <p>
     * 参考文档：https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2842586.html&renderType=iframe
     */
    @Test
    public void text2AudioTest() throws Exception {
        String message = """
                这这也不知道为啥哈，反正，它刚出来的时候儿叫台湾手抓饼，啊，现在就是可能这个，
                大陆这边儿都给改良了，整的都像那种，烙的那种，鸡蛋灌饼儿似的啦，啊，有就有那种感觉哈。
                """;
        System.out.println("【文字】: " + message);

        DashScopeAudioSpeechOptions speechSynthesisOptions = DashScopeAudioSpeechOptions.builder()
                .model(MODEL_COSYVOICE_V2) // 生成语音模型
                //.model(DashScopeSpeechSynthesisModel.DashScopeSpeechModel.SAMBERT_ZHICHU_V1.getModel())
                //.requestText(DashScopeSpeechSynthesisApi.RequestTextType.PLAIN_TEXT)
                //.responseFormat(DashScopeSpeechSynthesisApi.ResponseFormat.MP3)
                .voice("longyingda") // 音色人物：longyingcui（男）、longyingda（女）
                .build();

        SpeechSynthesisPrompt speechSynthesisPrompt =
                new SpeechSynthesisPrompt(message, speechSynthesisOptions);

        // 保存音频文件
        File file = new File(FILE_PATH + "/" + System.currentTimeMillis() + "_output.mp3");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            ByteBuffer byteBuffer = speechSynthesisModel.call(speechSynthesisPrompt)
                    .getResult().getOutput().getAudio();
            fos.write(byteBuffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 实时文生语音
     * <p>
     * 参考文档：https://bailian.console.aliyun.com/?tab=doc#/doc/?type=model&url=https%3A%2F%2Fhelp.aliyun.com%2Fdocument_detail%2F2842586.html&renderType=iframe
     */
    @Test
    public void streamText2AudioTest() throws Exception {
        String message = """
                这这也不知道为啥哈，反正，它刚出来的时候儿叫台湾手抓饼，啊，现在就是可能这个，
                大陆这边儿都给改良了，整的都像那种，烙的那种，鸡蛋灌饼儿似的啦，啊，有就有那种感觉哈。
                """;
        System.out.println("【文字】: " + message);

        DashScopeAudioSpeechOptions speechSynthesisOptions = DashScopeAudioSpeechOptions.builder()
                //.model(DashScopeSpeechSynthesisModel.DashScopeSpeechModel.SAMBERT_ZHICHU_V1.getModel())
                .model(MODEL_SAMBERT_ZHICHU_V1) // 生成语音模型
                //.requestText(DashScopeSpeechSynthesisApi.RequestTextType.PLAIN_TEXT)
                //.responseFormat(DashScopeSpeechSynthesisApi.ResponseFormat.MP3)
                .build();

        SpeechSynthesisPrompt speechSynthesisPrompt =
                new SpeechSynthesisPrompt(message, speechSynthesisOptions);

        Flux<SpeechSynthesisResponse> responseFlux = speechSynthesisModel.stream(speechSynthesisPrompt);


        // 保存音频文件
        CountDownLatch latch = new CountDownLatch(1);
        File file = new File(FILE_PATH + "/" + System.currentTimeMillis() + "_stream_output.mp3");
        try (FileOutputStream fos = new FileOutputStream(file)) {

            responseFlux.doFinally(signalType -> latch.countDown())
                    .subscribe(r -> {
                        ByteBuffer byteBuffer = r.getResult().getOutput().getAudio();
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes);
                        try {
                            fos.write(bytes);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            latch.await();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试 语音翻译
     */
    @Test
    public void audioTranscriptionTest() throws Exception {
        String audioUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav";

        DashScopeAudioTranscriptionOptions transcriptionOptions =
                DashScopeAudioTranscriptionOptions.builder()
                        .withModel(MODEL_SENSEVOICE_V1)
                        .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt
                (new UrlResource(audioUrl), transcriptionOptions);

        String output = audioTranscriptionModel.call(prompt).getResult().getOutput();

        System.out.println("语音翻译结果 = " + output);
    }

}
