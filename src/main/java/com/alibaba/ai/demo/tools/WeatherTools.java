
package com.alibaba.ai.demo.tools;

import com.alibaba.cloud.ai.toolcalling.weather.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 天气工具
 */
public class WeatherTools {

    private final WeatherService weatherService;

    public WeatherTools(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Tool(description = "Use api.weather to get weather information.")
    public String getWeather(@ToolParam(description = "City , days")
                             String city, int days) {
        return weatherService.apply(new WeatherService.Request(city, days)).city();
    }

}