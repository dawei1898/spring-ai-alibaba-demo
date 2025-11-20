package com.alibaba.ai.demo.tools;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

/**
 * 搜索工具
 *
 * @author dawei
 */
@Slf4j
public class SearchTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(String query, ToolContext toolContext) {
        log.info("调用 SearchTool 工具： query：{}", query);
        // TODO 待实现搜索逻辑
        return "搜索结果：" + query;
    }

}
