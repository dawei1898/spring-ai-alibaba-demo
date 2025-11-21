package com.alibaba.ai.demo.tools;

import com.alibaba.ai.demo.model.WeatherRequest;
import com.alibaba.ai.demo.model.WeatherResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * 使用 @Bean 定义 查询天气服务工具
 * @author dawei
 */
@Configuration(proxyBeanMethods = false)
public class MyWeatherTools {


    MyWeatherService myWeatherService  = new MyWeatherService();

    @Bean
    @Description("Get the weather in location")
    public Function<WeatherRequest, WeatherResponse> currentWeather() {
        return myWeatherService;
    }

}
