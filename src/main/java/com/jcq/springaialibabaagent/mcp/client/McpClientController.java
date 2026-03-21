package com.jcq.springaialibabaagent.mcp.client;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.Builder;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class McpClientController {

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @GetMapping("mcpTest")
    private void mcpTest() throws GraphRunnerException {

        ChatModel chatModel = getChatModel();

        ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();

        System.out.printf("""
                        =====Find the tools from spring ToolCallbackProvider=====
                        %s
                        """,
                JSON.toJSONString(toolCallbacks));

        // 构建智能体并绑定mcp服务
        ReactAgent agent = ReactAgent.builder()
                .name("ip_search")
                .model(chatModel)
                .description("你是一个天气查询助手")
                .saver(new MemorySaver())
                .toolCallbackProviders(toolCallbackProvider)
                        .build();

        // 运行时配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId("session")
                .build();

        // 流式调用agent
        Flux<NodeOutput> stream = agent.stream("上海未来7天天气怎么样", config);
        StringBuffer answerString = new StringBuffer();
        stream.doOnNext(output -> {
                    if (output.node().equals("_AGENT_MODEL_")) {
                        answerString.append(((StreamingOutput<?>) output).message().getText());
                    }
                    else if (output.node().equals("_AGENT_TOOL_")) {
                        answerString.append("\nTool Call:").append(((ToolResponseMessage) ((StreamingOutput<?>) output).message()).getResponses().get(0)).append("\n");
                    }
                })
                .doOnComplete(() -> System.out.println(answerString))
                .doOnError(e -> System.err.println("Stream Processing Error: " + e.getMessage()))
                .blockLast();
    }

    private static  ChatModel getChatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
    }

}
