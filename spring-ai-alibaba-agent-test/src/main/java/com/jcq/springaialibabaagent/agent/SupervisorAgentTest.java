package com.jcq.springaialibabaagent.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SupervisorAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public class SupervisorAgentTest {

    static final String SUPERVISOR_SYSTEM_PROMPT = """
            你是一个智能的内容管理监督者，负责协调和管理多个专业Agent来完成用户的内容处理需求。

            ## 你的职责
            1. 分析用户需求，将其分解为合适的子任务
            2. 根据任务特性，选择合适的Agent进行处理
            3. 监控任务执行状态，决定是否需要继续处理或完成任务
            4. 当所有任务完成时，返回FINISH结束流程

            ## 可用的子Agent及其职责

            ### writer_agent
            - **功能**: 擅长创作各类文章，包括散文、诗歌等文学作品
            - **适用场景**: 
            * 用户需要创作新文章、散文、诗歌等原创内容
            * 简单的写作任务，不需要后续评审或修改
            - **输出**: writer_output

            ### translator_agent
            - **功能**: 擅长将文章翻译成各种语言
            - **适用场景**: 当文章需要翻译成其他语言时
            - **输出**: translator_output

            ## 决策规则

            1. **单一任务判断**:
             - 如果用户只需要简单写作，选择 writer_agent
             - 如果用户需要翻译，选择 translator_agent

            2. **多步骤任务处理**:
             - 如果用户需求包含多个步骤（如"先写文章，然后翻译"），需要分步处理
             - 先路由到第一个合适的Agent，等待其完成
             - 完成后，根据剩余需求继续路由到下一个Agent
             - 直到所有步骤完成，返回FINISH

            3. **任务完成判断**:
             - 当用户的所有需求都已满足时，返回FINISH

            ## 响应格式
            只返回Agent名称（writer_agent、translator_agent）或FINISH，不要包含其他解释。
            """;

    public static void main(String[] args) throws GraphRunnerException, IOException {

        ChatModel chatModel = getChatModel();

        // 创建专业化的子Agent
        ReactAgent writerAgent = ReactAgent.builder()
                .name("writer_agent")
                .model(chatModel)
                .description("擅长创作各类文章，包括散文、诗歌等文学作品")
                .outputKey("writer_output")
                .build();

        ReactAgent translatorAgent = ReactAgent.builder()
                .name("translator_agent")
                .model(chatModel)
                .description("擅长将文章翻译成各种语言")
                .outputKey("translator_output")
                .build();

        // 创建监督者Agent
        SupervisorAgent supervisorAgent = SupervisorAgent.builder()
                .name("content_supervisor")
                .description("内容管理监督者")
                .systemPrompt(SUPERVISOR_SYSTEM_PROMPT)
                .mainAgent(writerAgent)
                .model(chatModel)
                .subAgents(List.of(writerAgent, translatorAgent))
                .build();

        // 使用 - 监督者会根据任务自动路由并支持多步骤处理
        Flux<NodeOutput> flux = supervisorAgent.stream("先帮我写一篇关于春天的短文,然后将文章翻译成英文");

        flux.subscribe(chunk -> {
            if (chunk instanceof StreamingOutput<?> streamingOutput) {
                String newContent = "";
                Message message = streamingOutput.message();
                if (message instanceof AssistantMessage assistantMessage) {
                    Object finishReason = message.getMetadata().get("finishReason");
                    if (finishReason == null) {
                        newContent = assistantMessage.getText();
                    }
                    if (finishReason != null && !finishReason.toString().equals("STOP")) {

                        newContent = assistantMessage.getText();
                    }
                }
                System.out.println(newContent);
            }
        });

        System.in.read();
    }

    public static ChatModel getChatModel() {

        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        // Note: model must be set when use options build.
                        .model(DashScopeChatModel.DEFAULT_MODEL_NAME)
                        .temperature(0.5)
                        .maxToken(1000)
                        .build())
                .build();
    }
}
