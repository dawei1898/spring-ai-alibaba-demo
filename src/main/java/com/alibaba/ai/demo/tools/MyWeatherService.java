package com.alibaba.ai.demo.tools;

import com.alibaba.ai.demo.enums.Uint;
import com.alibaba.ai.demo.model.WeatherRequest;
import com.alibaba.ai.demo.model.WeatherResponse;

import java.util.function.Function;



/**
 * 查询天气服务
 *
 * @author dawei
 */
public class MyWeatherService implements Function<WeatherRequest, WeatherResponse> {
    @Override
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        return new WeatherResponse(28.0, Uint.C);
    }
}

