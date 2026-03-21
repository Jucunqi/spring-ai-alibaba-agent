package com.jcq.server.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "根据城市名称获取天气信息")
    public String getWeatherByCity(String city) {
        return city + " 今天天气很好！";
    }
}
