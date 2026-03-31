package com.jcq.springaialibabaagent.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.AgentTool;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.Optional;

public class AgentToolTest {

    public static class JokeRequest {
        private String jokeType;
        private String audienceType;
        private int length;

        public String getJokeType() {
            return jokeType;
        }

        public void setJokeType(String jokeType) {
            this.jokeType = jokeType;
        }

        public String getAudienceType() {
            return audienceType;
        }

        public void setAudienceType(String audienceType) {
            this.audienceType = audienceType;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public static class JokeResponse {
        private String jokeType;
        private String content;
        private int length;

        public String getJokeType() {
            return jokeType;
        }

        public void setJokeType(String jokeType) {
            this.jokeType = jokeType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public static void main(String[] args) throws GraphRunnerException {

        // agentToolTest();

        // 使用 inputSchema 定义工具的输入格式
        // agentToolWithInputSchemaTest();

        // agentToolWithInputTypeTest();

        // agentToolWithOutputSchemaTest();

        // agentToolWithOutputTypeTest();

        // agentToolWithAllSchemaTypesTest();

        subAgentToolsTest();
    }

    private static void agentToolWithInputSchemaTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();
        String writerInputSchema = """
                {
                	"type": "object",
                	"properties": {
                		"jokeType": {
                			"type": "string",
                			"description": "笑话类型:cold-冷笑话, life-生活笑话"
                		},
                		"audienceType": {
                			"type": "string",
                			"description": "受众类型:adult-成人, kid-儿童"
                		},
                		"length": {
                			"type": "integer",
                			"description": "笑话大概字数"
                		}
                	},
                	"required": ["jokeType", "audienceType", "length"]
                }
                """;

        ReactAgent writerAgent = ReactAgent.builder()
                .name("structured_joke_agent")
                .model(chatModel)
                .description("根据结构化输入讲笑话")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。请严格按照输入的主题、受众类型和字数要求创作笑话。")
                .inputSchema(writerInputSchema)
                .build();

        ReactAgent talkAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("你需要调用讲笑话来完成用户的请求。")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent)))
                .build();

        Optional<OverAllState> result = talkAgent
                .invoke("讲一个关于小明小朋友的冷笑话，大约50字");

        System.out.println("=== Agent Tool with InputSchema Test ===");
        System.out.println(result.get());
    }

    private static void agentToolWithOutputSchemaTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();
        String writerOutputSchema = """
                {
                	"type": "object",
                	"properties": {
                		"jokeType": {
                			"type": "string",
                			"description": "笑话类型:cold-冷笑话, life-生活笑话"
                		},
                		"content": {
                			"type": "string",
                			"description": "笑话内容"
                		},
                		"length": {
                			"type": "integer",
                			"description": "笑话大概字数"
                		}
                	},
                	"required": ["jokeType", "content", "length"]
                }
                """;

        ReactAgent writerAgent = ReactAgent.builder()
                .name("structured_joke_agent")
                .model(chatModel)
                .description("输出结构化的笑话")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。请严格按照输入的主题、受众类型和字数要求创作笑话。")
                .outputSchema(writerOutputSchema)
                .build();

        ReactAgent talkAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("你需要调用讲笑话工具来完成用户的请求。")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent)))
                .build();

        Optional<OverAllState> result = talkAgent
                .invoke("讲一个关于小明小朋友的冷笑话，大约50字");

        System.out.println("=== Agent Tool with OutputSchema Test ===");
        System.out.println(result.get());
    }

    private static void agentToolWithInputTypeTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();

        ReactAgent writerAgent = ReactAgent.builder()
                .name("structured_joke_agent")
                .model(chatModel)
                .description("根据结构化输入讲笑话")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。请严格按照输入的主题、受众类型和字数要求创作笑话。")
                .inputType(JokeRequest.class)
                .build();

        ReactAgent talkAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("你需要调用讲笑话来完成用户的请求。")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent)))
                .build();

        Optional<OverAllState> result = talkAgent
                .invoke("讲一个关于小明小朋友的冷笑话，大约50字");

        System.out.println("=== Agent Tool with InputType Test ===");
        System.out.println(result.get());
    }

    private static void agentToolWithOutputTypeTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();

        ReactAgent writerAgent = ReactAgent.builder()
                .name("structured_joke_agent")
                .model(chatModel)
                .description("输出结构化的笑话")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。请严格按照输入的主题、受众类型和字数要求创作笑话。")
                .outputType(JokeResponse.class)
                .build();

        ReactAgent talkAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("你需要调用讲笑话来完成用户的请求。")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent)))
                .build();

        Optional<OverAllState> result = talkAgent
                .invoke("讲一个关于小明小朋友的冷笑话，大约50字");

        System.out.println("=== Agent Tool with OutputType Test ===");
        System.out.println(result.get());
    }

    private static void agentToolWithAllSchemaTypesTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();

        ReactAgent writerAgent = ReactAgent.builder()
                .name("structured_joke_agent")
                .model(chatModel)
                .description("完整结构化的讲笑话工具")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。请严格按照输入的主题、受众类型和字数要求创作笑话。")
                .inputType(JokeRequest.class)
                .outputType(JokeResponse.class)
                .build();

        ReactAgent talkAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("你需要调用讲笑话来完成用户的请求。")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent)))
                .build();

        Optional<OverAllState> result = talkAgent
                .invoke("讲一个关于小明小朋友的冷笑话，大约50字");

        System.out.println("=== Agent Tool with AllSchema Test ===");
        System.out.println(result.get());
    }



    private static void agentToolTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();
        ReactAgent writerAgent = ReactAgent.builder()
                .name("joke_agent")
                .model(chatModel)
                .description("负责讲笑话。")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。你的任务是根据用户的要求，讲一个轻松、健康、积极向上的短笑话。")
                .build();

        ReactAgent blogAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("根据用户的描述讲一个笑话。")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent)))
                .build();

        Optional<OverAllState> result = blogAgent
                .invoke(new UserMessage("给我讲一个关于小明的笑话"));


        System.out.println("=== Agent Tool Test Result===");
        System.out.println(result.get());
    }

    private static void subAgentToolsTest() throws GraphRunnerException {
        ChatModel chatModel = getChatModel();
        ReactAgent writerAgent = ReactAgent.builder()
                .name("joke_agent")
                .model(chatModel)
                .description("负责讲笑话。")
                .instruction("你是一个幽默风趣、反应敏捷的笑话智能体。你的任务是根据用户的要求，讲一个轻松、健康、积极向上的短笑话。")
                .enableLogging(true)
                .build();

        ReactAgent translateAgent = ReactAgent.builder()
                .name("translate_agent")
                .model(chatModel)
                .description("负责把笑话翻译成用户需要的语言。")
                .instruction("你是一个专业的翻译官。你的任务是把笑话翻译成用户需要的语言")
                .enableLogging(true)
                .build();

        ReactAgent talkAgent = ReactAgent.builder()
                .name("talk_agent")
                .model(chatModel)
                .instruction("你可以访问多个工具：讲笑话和翻译笑话，根据用户需求选择合适的工具来完成任务")
                .tools(List.of(AgentTool.getFunctionToolCallback(writerAgent),
                        AgentTool.getFunctionToolCallback(translateAgent)))
                .build();

        Optional<OverAllState> result = talkAgent
                .invoke(new UserMessage("给我讲一个关于小明的笑话，大概50字，并将其翻译成英文"));


        System.out.println("=== Sub Agents Tool Test Result===");
        System.out.println(result.get());
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
