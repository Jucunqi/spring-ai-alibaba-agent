package com.jcq.springaialibabaagent.mcp.client.config;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class McpConfiguration {

    @Bean
    public McpSyncHttpClientRequestCustomizer mcpSyncHttpClientRequestCustomizer() {

        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer sk-057910f507c8400f995663e8100cb59a");
        return new HeaderSyncHttpRequestCustomizer(headers);
    }
}
