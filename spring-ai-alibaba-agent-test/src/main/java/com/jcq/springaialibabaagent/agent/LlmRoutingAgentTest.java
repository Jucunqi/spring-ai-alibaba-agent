package com.jcq.springaialibabaagent.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static com.jcq.springaialibabaagent.tools.PoetTool.createPoetToolCallback;

public class LlmRoutingAgentTest {

    public static void main(String[] args) {

        ChatModel chatModel = getChatModel();
        ReactAgent proseWriterAgent = ReactAgent.builder()
                .name("prose_writer_agent")
                .model(chatModel)
                .description("Can write prose articles.")
                .instruction("You are a renowned writer skilled in writing prose. Please respond to the following request: {input}")
                .outputKey("prose_article")
                .build();

        ReactAgent poemWriterAgent = ReactAgent.builder()
                .name("poem_writer_agent")
                .model(chatModel)
                .description("Can write modern poetry.")
                .instruction("You are a famous poet skilled in modern poetry. Please use tools to respond to the following request: {input}")
                .outputKey("poem_article")
                .tools(List.of(createPoetToolCallback()))
                .build();

        LlmRoutingAgent blogAgent = LlmRoutingAgent.builder()
                .name("blog_agent")
                .model(chatModel)
                .description("Can write articles or poems based on user-provided topics.")
                .subAgents(List.of(proseWriterAgent, poemWriterAgent))
                .build();

        try {

            GraphRepresentation representation = blogAgent.getGraph().getGraph(GraphRepresentation.Type.PLANTUML);
            System.out.println(representation.content());

            Optional<OverAllState> result = blogAgent.invoke("帮我写一个100字左右的现代诗");
            blogAgent.invoke("帮我写一个100字左右的现代诗");
            Optional<OverAllState> result3 = blogAgent.invoke("帮我写一个100字左右的现代诗");


            OverAllState state = result.get();
            OverAllState state3 = result3.get();

            AssistantMessage poemContent = (AssistantMessage) state.value("poem_article").get();
            AssistantMessage poemContent3 = (AssistantMessage) state3.value("poem_article").get();

            System.out.println(result.get());
            System.out.println("------------------");
            System.out.println(result3.get());
        }
        catch (CompletionException | GraphRunnerException e) {
            e.printStackTrace();
        }
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
