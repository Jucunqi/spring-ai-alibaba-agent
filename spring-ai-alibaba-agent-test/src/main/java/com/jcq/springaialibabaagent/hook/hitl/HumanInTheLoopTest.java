package com.jcq.springaialibabaagent.hook.hitl;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.jcq.springaialibabaagent.tools.PoetTool.createPoetToolCallback;

public class HumanInTheLoopTest {

    public static void main(String[] args) throws GraphRunnerException {

        // 1. 构建model对象
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 2. 创建approvalOn函数
        Map<String, ToolConfig> approvalOn = Map.of(
                "poem", ToolConfig.builder().description("请确认诗歌工具执行").build()
        );

        // 3. 构建智能体对象
        ReactAgent agent = ReactAgent.builder()
                .name("single_agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .tools(List.of(createPoetToolCallback()))
                .hooks(HumanInTheLoopHook.builder().approvalOn(approvalOn).build())
                .outputKey("article")
                .build();

        // 4. 创建threadId 用户智能体上下文记忆
        String threadId = "user-session-001";
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        // 5.1. 第一次调用 - 触发中断
        System.out.println("=== 第一次调用：期望中断 ===");
        Optional<NodeOutput> result = agent.invokeAndGetOutput(
                "帮我写一首100字左右的诗",
                config
        );

        // 5.2 检查中断并处理
        if (result.isPresent() && result.get() instanceof InterruptionMetadata) {
            InterruptionMetadata interruptionMetadata = (InterruptionMetadata) result.get();

            System.out.println("检测到中断，需要人工审批");
            List<InterruptionMetadata.ToolFeedback> toolFeedbacks =
                    interruptionMetadata.toolFeedbacks();

            for (InterruptionMetadata.ToolFeedback feedback : toolFeedbacks) {
                System.out.println("id: " + feedback.getId());
                System.out.println("工具: " + feedback.getName());
                System.out.println("参数: " + feedback.getArguments());
                System.out.println("描述: " + feedback.getDescription());
            }

            // 5.3. 模拟人工决策（这里选择批准）
            InterruptionMetadata.Builder feedbackBuilder = InterruptionMetadata.builder()
                    .nodeId(interruptionMetadata.node())
                    .state(interruptionMetadata.state());

            toolFeedbacks.forEach(toolFeedback -> {
                InterruptionMetadata.ToolFeedback approvedFeedback =
                        InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                                .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                                .build();
                feedbackBuilder.addToolFeedback(approvedFeedback);
            });

            InterruptionMetadata approvalMetadata = feedbackBuilder.build();

            // 5.4 第二次调用 - 使用人工反馈恢复执行
            System.out.println(" === 第二次调用：使用批准决策恢复 ===");
            RunnableConfig resumeConfig = RunnableConfig.builder()
                    .threadId(threadId)
                    .addMetadata(RunnableConfig.HUMAN_FEEDBACK_METADATA_KEY, approvalMetadata)
                    .build();

            Optional<NodeOutput> finalResult = agent.invokeAndGetOutput("", resumeConfig);

            if (finalResult.isPresent()) {
                System.out.println("执行完成");
                Object article = finalResult.get().state().data().get("article");
                System.out.println("最终结果: " + article);
            }
        }
    }
}
