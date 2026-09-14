package com.view.data;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;

public class MainAllMessages {

    // Define the tool class
    static class CalculatorTools {
        @Tool("this show me how to add things")
        public int proceedAdd(
                @P("first number") int a,
                @P("second number") int b
        ) {
            return a + b;
        }
    }

    // Define the AI Service interface
    interface Assistant {
        Result<AiMessage> chat(String userMessage);
    }

    public static void main(String[] args) {
        // Reads OPENAI_API_KEY from environment variables by default
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o")
                .build();

        // Build the agent loop binding model + tool
        Assistant agent = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(new CalculatorTools())
                .build();

        // Invoke the agent
        Result<AiMessage> msgList = agent.chat("calculate the total 12 + 23 + 21 + 36");
        System.out.println(msgList.content().text());
        // Output: 12 added to 23 is 35.
    }
}
