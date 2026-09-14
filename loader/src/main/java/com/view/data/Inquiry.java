package com.view.data;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Inquiry {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/crt_vector";
    private static final String DB_USER = "aitest";
    private static final String DB_PASSWORD = "aitest";
    private static final String QUERY_TEXT = "what is stock and bonds allocation suggested";

    private static class Document {
        String id;
        String content;
        float[] embedding;
        double distance;

        Document(String id, String content, float[] embedding) {
            this.id = id;
            this.content = content;
            this.embedding = embedding;
        }
    }

    public static void main(String[] args) {
        try {
            new Inquiry().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        System.out.println("=== RAG Query ===");
        System.out.println("Query: " + QUERY_TEXT);
        System.out.println();

        // 1. Initialize embedding model
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-3-small")
                .dimensions(384)
                .build();

        // 2. Embed the query
        System.out.println("Embedding query...");
        var embeddingResponse = embeddingModel.embed(QUERY_TEXT);
        var embedding = embeddingResponse.content();
        float[] queryVector = embedding.vector();
        System.out.println("Query embedding: " + queryVector.length + " dimensions");

        // 3. Retrieve all documents and compute distances
        System.out.println("Searching database for similar documents...");
        List<Document> retrievedDocuments = retrieveSimilarDocuments(queryVector);
        System.out.println("Retrieved " + retrievedDocuments.size() + " documents");
        System.out.println();

        // Display retrieved documents with IDs and distances
        displayRetrievedDocuments(retrievedDocuments);
        System.out.println();

        // 4. Build context from retrieved documents
        String context = buildContext(retrievedDocuments);

        // 5. Initialize chat model
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o")
                .build();

        // 6. Query OpenAI with context
        System.out.println("Querying OpenAI with context...");
        String prompt = buildPrompt(context, QUERY_TEXT);
        String answer = chatModel.generate(prompt);

        // 7. Display result
        System.out.println("=== Answer ===");
        System.out.println(answer);
    }

    private List<Document> retrieveSimilarDocuments(float[] queryVector) throws Exception {
        List<Document> result = new ArrayList<>();

        String queryVectorStr = formatVector(queryVector);
        String sql = "SELECT id, content, VEC_DISTANCE_COSINE(embedding, VEC_FromText(?)) AS distance " +
                     "FROM document_vectors " +
                     "ORDER BY distance ASC " +
                     "LIMIT 3";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, queryVectorStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String content = rs.getString("content");
                    double distance = rs.getDouble("distance");

                    Document doc = new Document(id, content, null);
                    doc.distance = distance;
                    result.add(doc);
                }
            }
        }

        return result;
    }

    private void displayRetrievedDocuments(List<Document> documents) {
        System.out.println("=== Retrieved Documents ===");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            System.out.println("\n[" + (i + 1) + "] ID: " + doc.id);
            System.out.println("    Distance: " + String.format("%.6f", doc.distance));
            System.out.println("    Content:\n" + doc.content);
            System.out.println("    " + "—".repeat(80));
        }
        System.out.println();
    }

    private String buildContext(List<Document> documents) {
        StringBuilder sb = new StringBuilder();
        sb.append("Context from similar documents:\n\n");
        for (int i = 0; i < documents.size(); i++) {
            sb.append("Document ").append(i + 1).append(":\n");
            sb.append(documents.get(i).content).append("\n\n");
        }
        return sb.toString();
    }

    private String buildPrompt(String context, String question) {
        return context +
                "---\n\n" +
                "Question: " + question + "\n\n" +
                "Based on the context above, provide a thoughtful answer to the question.";
    }

    private String formatVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

}
