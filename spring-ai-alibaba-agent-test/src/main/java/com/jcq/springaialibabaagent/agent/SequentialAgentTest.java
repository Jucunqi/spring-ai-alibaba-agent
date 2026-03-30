package com.jcq.springaialibabaagent.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public class SequentialAgentTest {

    public static void main(String[] args) throws GraphRunnerException, IOException {

        ChatModel chatModel = getChatModel();
        // 创建专业化的子Agent
        ReactAgent writerAgent = ReactAgent.builder()
                .name("writer_agent")
                .model(chatModel)
                .description("专业写作Agent")
                .instruction("你是一个知名的作家，擅长写作和创作。请根据用户的提问进行回答：{input}。")
                .outputKey("article")
                .build();

        ReactAgent translateAgent = ReactAgent.builder()
                .name("translate_agent")
                .model(chatModel)
                .description("专业翻译Agent")
                .instruction("""
                         你是一个知名的翻译官，擅长对文章翻译。
                         待翻译文章：
                        {article}
                        最终只返回翻译后的文章。""")
                .outputKey("reviewed_article")
                .build();

        // 创建顺序Agent
        SequentialAgent blogAgent = SequentialAgent.builder()
                .name("blog_agent")
                .description("根据用户给定的主题写一篇文章，然后将文章交给翻译员进行翻译")
                .subAgents(List.of(writerAgent, translateAgent))
                .build();

        // 测试
        Optional<OverAllState> result = blogAgent.invoke("帮我写一个100字左右的散文，不用输出字数");

        if (result.isPresent()) {
            OverAllState state = result.get();

            // 访问第一个Agent的输出
            state.value("article").ifPresent(article -> {
                if (article instanceof AssistantMessage) {
                    System.out.println("原始文章: " + ((AssistantMessage) article).getText());
                }
            });

            // 访问第二个Agent的输出
            state.value("reviewed_article").ifPresent(reviewedArticle -> {
                if (reviewedArticle instanceof AssistantMessage) {
                    System.out.println("翻译后文章: " + ((AssistantMessage) reviewedArticle).getText());
                }
            });
        }

        // 流式输出
        // Flux<NodeOutput> flux = blogAgent.stream("帮我写一个100字左右的散文");
        //
        // flux.subscribe(chunk -> {
        //
        //     if (chunk instanceof StreamingOutput<?> streamingOutput) {
        //
        //         String newContent = "";
        //         Message message = streamingOutput.message();
        //         if (message instanceof AssistantMessage assistantMessage) {
        //             Object finishReason = message.getMetadata().get("finishReason");
        //             if (finishReason == null) {
        //                 newContent = assistantMessage.getText();
        //             }
        //             if (finishReason != null && !finishReason.toString().equals("STOP")) {
        //
        //                 newContent = assistantMessage.getText();
        //             }
        //         }
        //         System.out.println(newContent);
        //     }
        // });
        // System.in.read();
    }

    private static void test01() throws GraphRunnerException {



    }

    public static ChatModel getChatModel() {

        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        // Note: model must be set when use options build.
                        .model("qwen3.5-plus")
                        .temperature(0.5)
                        .maxToken(1000)
                        .build())
                .build();
    }
}
