package com.jcq.springaialibabaagent.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.jcq.springaialibabaagent.agent.resp.ResponseFormat;
import com.jcq.springaialibabaagent.agent.tools.UserLocationTool;
import com.jcq.springaialibabaagent.agent.tools.WeatherForLocationTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;

/**
 * 构建一个真实的Agent
 *
 * @author : jucunqi
 * @since : 2026/3/13
 */
public class RealAgent {

    public static void main(String[] args) throws GraphRunnerException {

        String SYSTEM_PROMPT = """
                你是一位擅长说**天气冷笑话/谐音梗**的专业天气预报员。
                            
                你可以使用两个工具：
                            
                - **get_weather_for_location**：用于获取指定地点的天气
                - **get_user_location**：用于获取用户当前所在位置
                            
                如果用户询问天气，**必须先确认地点**。
                如果从问题中能判断出他们指的是**自己所在的地方**，
                就使用 **get_user_location** 工具获取他们的位置。
                """;


        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AliQwen_API"))
                .build();

        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        // Note: model must be set when use options build.
                        .model(DashScopeChatModel.DEFAULT_MODEL_NAME)
                        .temperature(0.5)
                        .maxToken(1000)
                        .build())
                .build();

        // 创建工具回调
        ToolCallback getWeatherTool = FunctionToolCallback
                .builder("getWeatherForLocation", new WeatherForLocationTool())
                .description("获取一个给定城市的天气")
                .inputType(String.class)
                .build();

        ToolCallback getUserLocationTool = FunctionToolCallback
                .builder("getUserLocation", new UserLocationTool())
                .description("根据User Id获取用户位置")
                .inputType(String.class)
                .build();

        // 创建 agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_pun_agent")
                .model(chatModel)
                .tools(getWeatherTool, getUserLocationTool)
                .systemPrompt(SYSTEM_PROMPT)
                .outputType(ResponseFormat.class)
                .saver(new MemorySaver())
                .build();

        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(Thread.currentThread().getId() + "").build();

        // 第一次调用
        AssistantMessage response1 = agent.call("上海今天天气怎么样", runnableConfig);
        System.out.println(response1);

        // 第二次调用
        AssistantMessage response2 = agent.call("明天天气怎么样", runnableConfig);
        System.out.println(response2);
    }
}
