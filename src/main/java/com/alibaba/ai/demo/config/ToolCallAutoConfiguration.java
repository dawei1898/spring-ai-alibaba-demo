
package com.alibaba.ai.demo.config;

import com.alibaba.ai.demo.tools.TimeTools;
import com.alibaba.ai.demo.tools.WeatherTools;
import com.alibaba.cloud.ai.toolcalling.time.GetCurrentTimeByTimeZoneIdService;
import com.alibaba.cloud.ai.toolcalling.weather.WeatherService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具配置
 */
@Configuration
@ConditionalOnClass(GetCurrentTimeByTimeZoneIdService.class)
public class ToolCallAutoConfiguration {

    @Bean
    public TimeTools timeTools(GetCurrentTimeByTimeZoneIdService service) {
        return new TimeTools(service);
    }

    @Bean
    public WeatherTools weatherTools(WeatherService service) {
        return new WeatherTools(service);
    }



}