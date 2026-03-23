# Spring AI Alibaba Agent 示例项目

这是一个基于 Spring AI Alibaba Framework 构建的综合性示例项目，展示了如何构建具备多种能力的智能AI代理。项目包含Agent核心功能、MCP客户端/服务端、技能系统等模块。

## 项目概述

本项目演示了以下核心能力：
- **智能Agent构建**：基于ReAct模式的AI代理，支持工具调用和推理
- **MCP协议支持**：Model Context Protocol客户端和服务端实现
- **技能系统**：可扩展的技能注册和发现机制
- **多工具集成**：天气查询、Python代码执行、PDF处理等
- **流式响应**：支持SSE流式输出，提升用户体验

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.12-SNAPSHOT | 基础框架 |
| Spring AI Alibaba Agent Framework | 1.1.2.0 | AI代理框架 |
| Spring AI MCP | 1.1.2 | MCP协议支持 |
| DashScope | 最新版 | 通义千问模型 |
| GraalVM Polyglot | 24.2.1 | Python代码执行 |
| Java | 17+ | 编程语言 |
| Maven | 3.6+ | 构建工具 |

## 项目结构

```
spring-ai-alibaba-agent/
├── spring-ai-alibaba-agent-test/          # 主测试模块
│   ├── src/main/java/com/jcq/springaialibabaagent/
│   │   ├── agent/                         # Agent实现
│   │   │   ├── RealAgent.java            # 天气助手Agent示例
│   │   │   └── resp/ResponseFormat.java  # 结构化输出
│   │   ├── hook/                         # Agent钩子
│   │   │   └── hitl/HumanInTheLoopTest.java  # 人机交互循环
│   │   ├── mcp/client/                   # MCP客户端
│   │   │   ├── McpClientController.java  # MCP测试接口
│   │   │   └── config/                   # MCP配置
│   │   ├── skill/                        # 技能系统
│   │   │   └── AgentSkillTest.java       # 技能测试
│   │   ├── tools/                        # 工具实现
│   │   │   ├── PythonTool.java          # Python代码执行
│   │   │   ├── PoetTool.java            # 诗歌生成
│   │   │   ├── UserLocationTool.java    # 用户位置获取
│   │   │   └── WeatherForLocationTool.java  # 天气查询
│   │   └── SpringAiAlibabaAgentApplication.java
│   └── src/main/resources/
│       └── skills/                       # 技能定义目录
│           └── pdf-extractor/           # PDF提取技能
├── apring-ai-alibaba-mcp-server/         # MCP服务端模块
│   └── src/main/java/com/jcq/server/
│       ├── service/WeatherService.java   # 天气服务
│       └── config/McpServerConfig.java   # MCP服务端配置
└── pom.xml                               # 父POM
```

## 核心功能示例

### 1. 天气助手Agent

`RealAgent`展示了一个完整的天气查询Agent，具备以下特性：
- 使用系统提示词定义Agent角色和能力
- 集成天气查询和用户定位工具
- 支持结构化输出（ResponseFormat）
- 记忆功能保持对话上下文

```java
ReactAgent agent = ReactAgent.builder()
    .name("weather_pun_agent")
    .model(chatModel)
    .tools(getWeatherTool, getUserLocationTool)
    .systemPrompt(SYSTEM_PROMPT)
    .outputType(ResponseFormat.class)
    .saver(new MemorySaver())
    .build();
```

### 2. MCP客户端集成

`McpClientController`演示了如何通过MCP协议调用外部服务：
- 自动发现和加载MCP工具
- 流式响应处理
- 工具调用链可视化

```java
ReactAgent agent = ReactAgent.builder()
    .name("ip_search")
    .model(chatModel)
    .toolCallbackProviders(toolCallbackProvider)
    .build();

// 流式调用
Flux<NodeOutput> stream = agent.stream("上海未来7天天气怎么样", config);
```

### 3. 技能系统

`AgentSkillTest`展示了基于Markdown的技能定义和动态加载：
- 从类路径自动发现技能
- 技能包含完整的执行指令和错误处理
- 支持Shell工具和Python工具的组合使用

### 4. 自定义工具

项目包含多种工具实现：

| 工具 | 功能 | 技术亮点 |
|------|------|----------|
| PythonTool | 执行Python代码 | GraalVM沙箱环境 |
| PoetTool | 生成诗歌 | 固定内容返回 |
| WeatherForLocationTool | 查询天气 | 模拟数据演示 |
| UserLocationTool | 获取用户位置 | 上下文感知 |

## 快速开始

### 环境准备

1. **Java环境**
   ```bash
   java -version  # 需要17或更高版本
   ```

2. **配置API Key**
   ```bash
   export AliQwen_API=your_dashscope_api_key
   ```

3. **安装GraalVM（可选，用于Python工具）**
   - 下载并安装GraalVM CE 24.2.1
   - 配置`JAVA_HOME`环境变量

### 运行项目

```bash
# 克隆项目
git clone <repository-url>
cd spring-ai-alibaba-agent

# 编译安装
./mvnw clean install

# 运行主模块
cd spring-ai-alibaba-agent-test
./mvnw spring-boot:run
```

### 测试示例

#### 1. 测试天气Agent
运行 `RealAgent.main()` 方法，Agent会根据你的位置返回带冷笑话的天气预报。

#### 2. 测试MCP客户端
启动应用后访问：
```bash
curl http://localhost:8080/mcpTest
```

#### 3. 测试技能系统
运行 `AgentSkillTest.main()` 方法，体验PDF提取技能。

## 配置说明

### application.yml

```yaml
spring:
  application:
    name: spring-ai-alibaba-agent
  ai:
    mcp:
      client:
        enabled: true
        request-timeout: 30s
        stdio:
          servers-configuration: classpath:mcp-servers.json
```

### MCP服务端配置

在 `mcp-servers.json` 中配置MCP服务端：

```json
{
  "mcpServers": {
    "weather": {
      "command": "java",
      "args": ["-jar", "weather-server.jar"]
    }
  }
}
```

## 扩展开发

### 添加新工具

1. 实现 `BiFunction` 接口：

```java
@Component
public class MyTool implements BiFunction<MyRequest, ToolContext, String> {
    @Override
    public String apply(MyRequest request, ToolContext context) {
        // 工具逻辑
        return result;
    }
}
```

2. 注册到Agent：

```java
ToolCallback myTool = FunctionToolCallback
    .builder("myTool", new MyTool())
    .description("工具描述")
    .inputType(MyRequest.class)
    .build();
```

### 添加新技能

1. 在 `src/main/resources/skills/` 下创建技能目录
2. 编写 `SKILL.md` 定义技能指令
3. 添加必要的脚本文件

### 自定义Agent

```java
ReactAgent agent = ReactAgent.builder()
    .name("custom-agent")
    .model(chatModel)
    .systemPrompt("自定义系统提示词")
    .tools(tool1, tool2)
    .saver(new MemorySaver())
    .enableLogging(true)
    .build();
```

## 相关资源

- [Spring AI Alibaba 官方文档](https://github.com/alibaba/spring-ai-alibaba)
- [通义千问 API 文档](https://help.aliyun.com/document_detail/2399480.html)
- [MCP协议规范](https://modelcontextprotocol.io/)
- [GraalVM Polyglot文档](https://www.graalvm.org/reference-manual/polyglot-programming/)



**注意**：本项目为示例性质，用于学习和演示 Spring AI Alibaba 相关功能。生产环境使用前请进行充分测试。
