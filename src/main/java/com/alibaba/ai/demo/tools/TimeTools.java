
package com.alibaba.ai.demo.tools;

import com.alibaba.cloud.ai.toolcalling.time.GetTimeByZoneIdService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 时间工具
 */
public class TimeTools {

    private final GetTimeByZoneIdService timeService;

    public TimeTools(GetTimeByZoneIdService timeService) {
        this.timeService = timeService;
    }

    @Tool(description = "Get the time of a specified city.")
    public String getCityTime(@ToolParam(description = "Time zone id, such as Asia/Shanghai, default is Asia/Shanghai")
                                    String timeZoneId) {
        return timeService.apply(new GetTimeByZoneIdService.Request(timeZoneId)).description();
    }

}