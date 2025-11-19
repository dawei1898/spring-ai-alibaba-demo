package com.alibaba.ai.demo.test.image;


import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;

import org.junit.jupiter.api.Test;
import org.springframework.ai.image.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


/**
 * 测试 图像生成
 *
 * @author dawei
 */

@SpringBootTest
@ActiveProfiles("local")
public class ImageModelTest {

    // 图像生成模型
    public static final String MODEL_WANX22_T2I_FLASH = "wan2.2-t2i-flash";
    public static final String MODEL_WANX22_T2I_PLUS = "wan2.2-t2i-plus";
    public static final String MODEL_WANX20_T2I_TURBO = "wanx2.0-t2i-turbo";


    @Autowired
    private DashScopeImageModel imageModel;

    @Test
    public void test1() throws Exception {
        String message = "画一只可爱的小狗";
        System.out.println("【提问】: " + message);

        ImagePrompt imagePrompt = new ImagePrompt(message);

        ImageResponse response =  imageModel.call(imagePrompt);
        String url = response.getResult().getOutput().getUrl();
        System.out.println("【生成图片】 = " + url);
    }

    @Test
    public void test2() throws Exception {
        String message = "一架隐身战斗机从天空飞过";
        System.out.println("【提问】: " + message);

        // 选择模型
        DashScopeImageOptions imageOptions = DashScopeImageOptions.builder()
                .withModel(MODEL_WANX20_T2I_TURBO)
                .withN( 1) // 生成图片数量
                .withWidth(512) // 图片宽度
                .withHeight(512) // 图片高度
                .withResponseFormat("url") // 返回图片格式
                .build();

        // 提问信息
        ImageMessage imageMessage = new ImageMessage(message);

        ImagePrompt imagePrompt = new ImagePrompt(imageMessage, imageOptions);

        ImageResponse response =  imageModel.call(imagePrompt);
        String url = response.getResult().getOutput().getUrl();
        System.out.println("【生成图片】 = " + url);
    }
}
