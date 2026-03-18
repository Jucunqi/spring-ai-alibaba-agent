package com.jcq.springaialibabaagent.skill;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.jcq.springaialibabaagent.tools.PythonTool;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class AgentSkillTest {

    public static void main(String[] args) throws Exception {

        // 1. 查找技能
        findSkills();

        // 2. 使用技能
        // useSkills();
    }

    private static void findSkills() throws GraphRunnerException, IOException {

        //1. 构建model对象
        ChatModel chatModel = getChatModel();

        //2. 构建hook对象
        SkillsAgentHook hook = getSkillsAgentHook();

        //3. 构建智能体对象
        ReactAgent agent = ReactAgent.builder()
                .name("skills-agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .hooks(List.of(hook))
                .build();

        //4. 调用智能体
        // AssistantMessage resp = agent.call("请介绍你有哪些技能");
        // System.out.println(resp.getText());
        Flux<NodeOutput> flux = agent.stream("请介绍你有哪些技能");

        flux.subscribe(output ->
        {
            if (output instanceof StreamingOutput<?> streamingOutput) {

                // System.out.println("=== NodeOutput ===");
                Message message = streamingOutput.message();
                if (message instanceof AssistantMessage assistantMessage) {
                    Object finishReason = message.getMetadata().get("finishReason");
                    if (finishReason != null && !finishReason.toString().equals("STOP")) {

                        System.out.println(assistantMessage.getText());
                    }
                }
                // System.out.println("Agent: " + streamingOutput.agent());
                // System.out.println("TokenUsage: " + streamingOutput.tokenUsage());

            }
            // System.out.println(output.);
            // System.out.println(output.state().value("messages"));

        });
        System.in.read();
    }

    /**
     * 获取技能智能体钩子。
     *
     * 配置技能注册中心为类路径下的 /skills 目录。
     */
    private static SkillsAgentHook getSkillsAgentHook() {
        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();

        return SkillsAgentHook.builder()
                .skillRegistry(registry)
                .build();
    }

    /**
     * 获取测试技能目录的绝对路径。
     *
     * 使用 ClassPathResource 在测试资源中定位技能目录。
     */
    private static String getTestSkillsDirectory() throws Exception {
        Resource resource = new ClassPathResource("skills");
        if (resource.exists() && resource.getFile().isDirectory()) {
            return resource.getFile().getAbsolutePath();
        }
        // Fallback: try to get from classloader
        URL url = AgentSkillTest.class.getClassLoader().getResource("skills");
        if (url != null && "file".equals(url.getProtocol())) {
            return Path.of(url.toURI()).toString();
        }
        throw new IllegalStateException("Cannot find test skills directory");
    }

    @NotNull
    private static ChatModel getChatModel() {
        // 1. 构建model对象
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
    }

    private static void useSkills() throws Exception {

        //1. 构建model对象
        ChatModel chatModel = getChatModel();

        //2. 构建hook对象
        SkillsAgentHook skillsHook = getSkillsAgentHook();

        //3. 构建shell工具钩子
        ShellToolAgentHook shellHook = ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(System.getProperty("user.dir")).build())
                .build();

        //4. 构建智能体对象
        ReactAgent agent = ReactAgent.builder()
                .name("skills-integration-agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(PythonTool.createPythonToolCallback(PythonTool.DESCRIPTION))
                .hooks(List.of(skillsHook, shellHook))
                .enableLogging(true)
                .build();

        // 5. 调用智能体
        String path = getTestSkillsDirectory() + "/pdf-extractor/skill-test.pdf";
        AssistantMessage response = agent.call(String.format("请从 %s 文件中提取关键信息。", path));

        // 6. 打印响应
        System.out.println("==========");
        System.out.println(response.getText());
    }
}
