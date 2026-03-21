package com.jcq.springaialibabaagent.mcp.client.config;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

public class HeaderSyncHttpRequestCustomizer implements McpSyncHttpClientRequestCustomizer {

    private final Map<String, String> headers;

    public HeaderSyncHttpRequestCustomizer(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void customize(HttpRequest.Builder builder, String method, URI endpoint, String body, McpTransportContext context) {
        headers.forEach(builder::header);
    }
}
