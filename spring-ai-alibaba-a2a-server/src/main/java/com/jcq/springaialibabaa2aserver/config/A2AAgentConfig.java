package com.jcq.springaialibabaa2aserver.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class A2AAgentConfig {

    @Bean(name = "jokeAgent")
    public ReactAgent jokeAgent() {

        // 获取ChatModel对象
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        // Note: model must be set when use options build.
                        .model(DashScopeChatModel.DEFAULT_MODEL_NAME)
                        .temperature(0.5)
                        .maxToken(1000)
                        .build())
                .build();

        // 创建智能体
        return ReactAgent.builder()
                .name("jokeAgent")
                .model(chatModel)
                .description("负责讲笑话。")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。你的任务是根据用户的要求，讲一个轻松、健康、积极向上的短笑话。")
                .build();
    }
}
