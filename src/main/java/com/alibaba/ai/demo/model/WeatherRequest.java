package com.alibaba.ai.demo.model;

import com.alibaba.ai.demo.enums.Uint;
import org.springframework.ai.tool.annotation.ToolParam;

public record WeatherRequest(
        @ToolParam(description = "The name of a city or a country") String location,
        @ToolParam(description = "Temperature unit preference") Uint uint) {}