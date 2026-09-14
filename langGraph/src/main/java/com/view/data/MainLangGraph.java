package com.view.data;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class MainLangGraph {

    public static class CustomState extends AgentState {
        public CustomState(Map<String, Object> init) {
            super(init);
        }

        public String getMessage() {
            return (String) data().get("message");
        }
    }

    public static void main(String[] args) throws Exception {

        // NodeAction interface methods return Map<String, Object>
        NodeAction<CustomState> greetingNode = state -> {
            String current = state.getMessage();
            System.out.println("calling greeting... ");

            return Map.of("message", "Hey, " + current + ", how is your day so far?");
        };

        NodeAction<CustomState> secondCallNode = state -> {
            String current = state.getMessage();
            System.out.println("calling secondCall... ");

            return Map.of("message", current + " has the suffix secondCall");
        };

        StateGraph<CustomState> graph = new StateGraph<>(CustomState::new);

        // Wrap synchronous NodeAction with node_async
        graph.addNode("greeter", node_async(greetingNode));
        graph.addNode("secondCall", node_async(secondCallNode));

        graph.addEdge(START, "greeter");
        graph.addEdge("greeter", "secondCall");
        graph.addEdge("secondCall", END);

        CompiledGraph<CustomState> app = graph.compile();

        // app.invoke returns Optional<CustomState>
        CustomState result = app.invoke(Map.of("message", "Bob"))
                .orElseThrow(() -> new RuntimeException("Graph execution failed"));

        System.out.println(result.data());
    }
}