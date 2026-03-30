package com.jcq.springaialibabaagent.strategy;

import com.alibaba.cloud.ai.graph.GraphResponse;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.flow.agent.ParallelAgent;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.HashMap;
import java.util.Map;

public class CustomMergeStrategy implements ParallelAgent.MergeStrategy {

    @Override
    public Object merge(Map<String, Object> mergedState, OverAllState state) {
        // 从每个Agent的状态中提取输出
        state.data().forEach((key, value) -> {

            // 检查key不为null且以"_result"结尾
            if (key != null && key.endsWith("_result")) {
                String resultText = "";
                if (value instanceof GraphResponse graphResponse) {
                    if (graphResponse.resultValue().isPresent()) {
                        HashMap messageMap = (HashMap) graphResponse.resultValue().get();
                        AssistantMessage assistantMessage = (AssistantMessage) messageMap.get(key);
                        resultText = assistantMessage.getText();
                    }
                } else if (value != null) {
                    resultText = value.toString();
                }
                Object existing = mergedState.get("all_results");
                if (existing == null) {
                    mergedState.put("all_results", resultText);
                }
                else {
                    mergedState.put("all_results", existing + "\n\n---\n\n" + resultText);
                }
            }
        });
        return mergedState;
    }
}
