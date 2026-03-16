# Spring AI Alibaba Agent 示例项目

这是一个基于 Spring AI Alibaba Framework 构建的示例项目，用于演示和测试 Spring AI Alibaba 相关服务的使用。项目功能正在持续更新中。

## 项目概述

本项目旨在：
- 演示 Spring AI Alibaba Agent Framework 的基本使用方法
- 提供一个可扩展的 Agent 开发框架
- 集成通义千问模型进行智能对话
- 展示工具集成和人机交互循环的实现

## 技术栈

- **Spring Boot** 3.5.12-SNAPSHOT
- **Spring AI Alibaba Agent Framework** 1.1.2.0
- **DashScope** 通义千问模型
- **Java** 17
- **Maven** 构建工具

## 项目结构

```
spring-ai-alibaba-agent/
├── src/main/java/com/jcq/springaialibabaagent/
│   ├── agent/            # Agent 实现
│   ├── hook/             # 钩子（如人机交互循环）
│   ├── tools/            # 工具实现
│   └── SpringAiAlibabaAgentApplication.java # 启动类
├── src/main/resources/
│   └── application.properties        # 配置文件
├── pom.xml                           # Maven 配置
└── README.md                         # 项目说明
```

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6+
- 通义千问 API Key

### 配置步骤

1. **配置 API Key**
   
   设置环境变量：
   ```bash
   export AliQwen_API=your_api_key_here
   ```

2. **运行项目**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **测试 Agent**
   运行 `RealAgent` 类中的主方法进行测试。

## 核心功能

- **AI Agent 构建**：使用 Spring AI Alibaba Agent Framework 构建智能代理
- **工具集成**：演示如何集成自定义工具
- **人机交互**：支持人机交互循环（HITL）
- **模型配置**：展示如何配置和使用通义千问模型

## 配置说明

### 应用配置

在 `application.properties` 中配置应用基本信息：

```properties
spring.application.name=spring-ai-alibaba-agent
```

### 模型配置

在代码中配置通义千问模型参数：

- 模型名称
- 温度参数
- 最大令牌数
- API Key

## 扩展开发

- **添加新工具**：实现 `BiFunction` 接口并在 Agent 中注册
- **自定义钩子**：实现各种钩子来扩展 Agent 功能
- **模型切换**：根据需要切换不同的 AI 模型

## 注意事项

- **API Key 安全**：不要将 API Key 提交到代码仓库
- **资源使用**：注意 API 调用频率和费用
- **功能更新**：项目功能正在持续开发和更新中

## 相关链接

- [Spring AI Alibaba 官方文档](https://github.com/alibaba/spring-ai-alibaba)
- [通义千问 API 文档](https://help.aliyun.com/document_detail/2399480.html)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

---

**说明**：本项目为示例性质，用于学习和测试 Spring AI Alibaba 相关功能，功能正在持续更新中。