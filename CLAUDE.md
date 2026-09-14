# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**aichat** is a Java application that demonstrates an AI agent framework built with LangChain4j. The agent integrates with OpenAI's GPT-4o model and can execute tools (e.g., calculations) in response to user requests.

### Key Technology Stack
- **Language**: Java
- **Build**: Maven
- **AI Framework**: LangChain4j (0.35.0)
- **LLM Provider**: OpenAI (GPT-4o via `langchain4j-open-ai`)

## Architecture

### Core Pattern: Agent + Tools

The codebase follows the **AI agent with tool use** pattern:

1. **LLM Model Layer** (`OpenAiChatModel`): Communicates with OpenAI GPT-4o
2. **Tool Definitions** (e.g., `CalculatorTools`): Classes decorated with `@Tool` that expose functions the agent can call
3. **Service Interface** (e.g., `Assistant`): Defines the contract for agent interactions (methods like `chat()`)
4. **Agent Binding** (`AiServices`): Wires the model, tools, and service interface together into an executable agent

### How It Works

When you invoke `agent.chat(userMessage)`:
1. The user message goes to the LLM
2. The LLM can recognize that a tool should be called (e.g., `proceedAdd`)
3. LangChain4j routes the tool call to the implementation
4. The tool executes and returns a result
5. The LLM synthesizes a natural language response

## Development Commands

### Build
```bash
cd aichat
mvn clean install
```

### Run the Main Application
```bash
cd aichat
mvn exec:java -Dexec.mainClass="com.view.data.MainAllMessages"
```

### Run Tests
```bash
cd aichat
mvn test
```

### Run a Single Test
```bash
cd aichat
mvn test -Dtest=YourTestClassName
```

## Environment Setup

### Required Environment Variables

- **`OPENAI_API_KEY`**: Your OpenAI API key. The application reads this in `Main.java` with `System.getenv("OPENAI_API_KEY")`.

```bash
# Before running, set the environment variable:
export OPENAI_API_KEY=sk-...  # macOS/Linux
$env:OPENAI_API_KEY = "sk-..."  # PowerShell
set OPENAI_API_KEY=sk-...  # Windows CMD
```

## Extending the Codebase

### Adding a New Tool

1. **Add a method** to the tool class (or create a new one) with the `@Tool` annotation:
   ```java
   @Tool("Brief description of what this tool does")
   public ReturnType methodName(
       @P("param description") String param1,
       @P("param description") int param2
   ) {
       // implementation
       return result;
   }
   ```

2. **Register the tool** in `AiServices.builder()`:
   ```java
   Assistant agent = AiServices.builder(Assistant.class)
       .chatLanguageModel(model)
       .tools(new CalculatorTools())
       .tools(new YourNewToolClass())  // Add here
       .build();
   ```

3. The agent will now be able to discover and call your new tool.

### Expanding the Service Interface

Add new methods to the `Assistant` interface to support different interaction patterns:
```java
interface Assistant {
    String chat(String userMessage);
    String askQuestion(String question, String context);
    // etc.
}
```

Each method is a potential "mode" the agent can execute.

### Dependency Updates

Edit `pom.xml` to update versions or add new LangChain4j integrations (e.g., embedding providers, vector stores). Common additions:
- `langchain4j-weaviate` for vector search
- `langchain4j-ollama` for local LLM fallback
- `langchain4j-community` for additional integrations

## Project Structure

```
aichat/
├── src/main/java/com/view/data/
│   └── Main.java              # Single entry point with agent setup
├── src/test/java/              # Unit tests (currently empty)
├── pom.xml                      # Maven configuration
└── target/                      # Build artifacts
```

## Common Patterns & Notes

- **Error Handling**: The current code expects a valid API key and model availability. Add try-catch blocks if implementing retry logic or handling API failures.
- **Tool Scope**: Keep tools focused and simple. LangChain4j works best when tools have clear, well-documented purposes.
- **Prompt Engineering**: The `@Tool` description strings are sent to the LLM, so make them clear and specific. This affects whether the agent chooses to use a tool.
- **Model Selection**: Currently set to `gpt-4o`. You can swap to other OpenAI models (e.g., `gpt-4-turbo`, `gpt-3.5-turbo`) by changing `modelName()`.
