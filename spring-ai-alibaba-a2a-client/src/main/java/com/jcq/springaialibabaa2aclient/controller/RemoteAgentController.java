package com.jcq.springaialibabaa2aclient.controller;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.agent.a2a.AgentCardProvider;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class RemoteAgentController {

    @Resource
    private AgentCardProvider agentCardProvider;

    @GetMapping("test")
    public void test() throws GraphRunnerException {

        // 服务发现：通过AgentCardProvider 从注册中心获取Agent
        A2aRemoteAgent remoteAgent = A2aRemoteAgent.builder()
                .name("jokeAgent")
                .agentCardProvider(agentCardProvider)
                .description("可以给我讲笑话")
                .build();

        Optional<OverAllState> result = remoteAgent.invoke("请给我讲一个关于小明的笑话，大概50字");

        result.ifPresent(state -> System.out.println(state.data().get("output")));
    }
}
