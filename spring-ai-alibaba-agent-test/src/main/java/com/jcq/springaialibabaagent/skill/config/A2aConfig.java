package com.jcq.springaialibabaagent.skill.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class A2aConfig {

    @Bean
    public ReactAgent skillAgent() {

        //1. 构建model对象
        ChatModel chatModel = getChatModel();

        //2. 构建hook对象
        SkillsAgentHook hook = getSkillsAgentHook();

        //3. 构建智能体对象
        return ReactAgent.builder()
                .name("skillsAgent")
                .model(chatModel)
                .saver(new MemorySaver())
                .hooks(List.of(hook))
                .build();
    }

    private static SkillsAgentHook getSkillsAgentHook() {
        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();

        return SkillsAgentHook.builder()
                .skillRegistry(registry)
                .build();
    }
    private static ChatModel getChatModel() {
        // 1. 构建model对象
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
    }

}
