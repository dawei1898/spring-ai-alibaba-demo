package com.alibaba.cloud.cloud.ai.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * @author dawei
 */
@Service
public class WeatherService {

    @Tool(description = "Get weather information by city name")
    public String getWeather(String cityName) {
        // Implementation
        return cityName + ", 今天多云、小雨";
    }

}
