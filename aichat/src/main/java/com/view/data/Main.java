package com.view.data;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

public class Main {

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
        String chat(String userMessage);
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
        String response = agent.chat("Please see if you can add 12 and 23");

        System.out.println(response);
        // Output: 12 added to 23 is 35.
    }
}
