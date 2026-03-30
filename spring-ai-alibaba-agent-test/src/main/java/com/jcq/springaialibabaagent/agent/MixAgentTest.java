package com.jcq.springaialibabaagent.agent;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.SequentialAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.model.ChatModel;

import java.util.List;
import java.util.Optional;

public class MixAgentTest {

    public static void main(String[] args) throws GraphRunnerException {

        ChatModel chatModel = getChatModel();
        // 1. 创建并行查询Agent（同时查用户信息 + 订单信息）
        ReactAgent userQueryAgent = ReactAgent.builder()
                .name("user_query")
                .model(chatModel)
                .description("查询用户信息、会员等级、历史行为")
                .instruction("根据用户ID查询用户信息：{input}")
                .outputKey("user_info")
                .build();

        ReactAgent orderQueryAgent = ReactAgent.builder()
                .name("order_query")
                .model(chatModel)
                .description("查询订单状态、物流、商品信息")
                .instruction("根据订单号查询订单详细信息：{input}")
                .outputKey("order_info")
                .build();

        // 并行执行：同时查用户+订单，大幅提速
        ParallelAgent queryAgent = ParallelAgent.builder()
                .name("parallel_query")
                .description("并行查询用户信息与订单信息")
                .subAgents(List.of(userQueryAgent, orderQueryAgent))
                .mergeOutputKey("query_data")
                .build();

        // 2. 智能分析Agent（分析问题类型：退款/换货/补发/咨询）
        ReactAgent analysisAgent = ReactAgent.builder()
                .name("order_analysis")
                .model(chatModel)
                .description("分析用户问题、订单状态，给出处理建议")
                .instruction("""
                          分析以下查询数据，判断用户需求类型：
                          1. 未发货仅退款 → 退款
                          2. 已收货质量问题 → 换货
                          3. 少发/漏发 → 补发
                          4. 其他 → 人工客服
                          数据：{query_data}
                        """)
                .outputKey("analysis_result")
                .build();

        // 3. 路由处理Agent（根据分析结果，自动选择处理方式）
        ReactAgent refundAgent = ReactAgent.builder()
                .name("refund_process")
                .model(chatModel)
                .description("自动生成退款方案")
                .instruction("""
                          根据订单与分析结果生成退款处理方案：
                          订单信息：{order_info}
                          分析结果：{analysis_result}
                        """)
                .outputKey("refund_solution")
                .build();

        ReactAgent exchangeAgent = ReactAgent.builder()
                .name("exchange_process")
                .model(chatModel)
                .description("自动生成换货方案")
                .instruction("""
                          根据订单与分析结果生成换货处理方案：
                          订单信息：{order_info}
                          分析结果：{analysis_result}
                        """)
                .outputKey("exchange_solution")
                .build();

        ReactAgent resendAgent = ReactAgent.builder()
                .name("resend_process")
                .model(chatModel)
                .description("自动生成补发方案")
                .instruction("""
                          根据订单与分析结果生成补发处理方案：
                          订单信息：{order_info}
                          分析结果：{analysis_result}
                        """)
                .outputKey("resend_solution")
                .build();

        // LLM智能路由：自动选最合适的处理Agent
        LlmRoutingAgent processAgent = LlmRoutingAgent.builder()
                .name("process_router")
                .description("根据分析结果智能路由到对应处理流程")
                .model(chatModel)
                .subAgents(List.of(refundAgent, exchangeAgent, resendAgent))
                .build();

        // 4. 组合成完整工作流（顺序执行）
        SequentialAgent orderWorkflow = SequentialAgent.builder()
                .name("ecommerce_order_workflow")
                .description("电商订单智能处理全流程：并行查询 → 智能分析 → 路由处理")
                .subAgents(List.of(queryAgent, analysisAgent, processAgent))
                .build();

        // 使用：传入用户问题 + 订单号
        Optional<OverAllState> result = orderWorkflow.invoke("订单号：123456，我要退款，商品还没发货");
        
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
