package com.view.data;

import com.github.f4b6a3.uuid.UuidCreator;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Load {
    public static void main(String[] args) {
        try {
            new Load().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        // 1. Read the markdown file
        Path infoPath = Path.of("C:\\Users\\dgabr\\IdeaProjects\\ai\\loader\\doc\\info.md");
        String content = Files.readString(infoPath);
        System.out.println("Read " + content.length() + " characters from info.md");

        // 2. Create a document and split into chunks
        Document document = Document.from(content);
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.split(document);
        System.out.println("Created " + segments.size() + " chunks");

        // 3. Initialize OpenAiEmbeddingModel
        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-3-small")
                .dimensions(384)
                .build();

        // 4. Generate SQL insert statements
        StringBuilder sqlBuilder = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);

            // Get embedding for this chunk
            var embeddingResponse = embeddingModel.embed(segment.text());
            var embedding = embeddingResponse.content();
            float[] vector = embedding.vector();

            // Generate UUID v7
            String id = UuidCreator.getTimeOrderedEpoch().toString();

            // Escape content for SQL (escape single quotes)
            String escapedContent = segment.text().replace("'", "''");

            // Format embedding as vector literal
            String embeddingArray = formatVector(vector);

            // Build INSERT statement
            String insertStatement = String.format(
                    "INSERT INTO document_vectors (id, content, embedding) VALUES ('%s', '%s', VEC_FromText('%s'));%n",
                    id, escapedContent, embeddingArray
            );

            sqlBuilder.append(insertStatement);

            if ((i + 1) % 5 == 0) {
                System.out.println("Embedded " + (i + 1) + "/" + segments.size() + " chunks...");
            }
        }

        // 5. Write to ingestion.sql
        Path outputPath = Path.of("C:\\Users\\dgabr\\IdeaProjects\\ai\\loader\\doc\\ingestion.sql");
        Files.writeString(outputPath, sqlBuilder.toString());

        System.out.println("✓ Generated " + segments.size() + " INSERT statements");
        System.out.println("✓ SQL written to: " + outputPath.toAbsolutePath());
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
