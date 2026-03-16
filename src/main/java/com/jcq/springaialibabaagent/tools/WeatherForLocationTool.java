package com.jcq.springaialibabaagent.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.function.BiFunction;

public class WeatherForLocationTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
            @ToolParam(description = "城市的名称") String city,
            ToolContext toolContext) {
        return  city + "的天气一直非常好！";
    }
}
