# AI Project Suite

A collection of Java applications demonstrating AI integration with LangChain4j, OpenAI GPT models, and vector databases. This project explores agent frameworks, embeddings, and retrieval-augmented generation (RAG) patterns.

## Modules

### 1. **aichat**
A foundational AI agent framework using LangChain4j and OpenAI GPT-4o. Demonstrates the core pattern of binding an LLM model with tool definitions to create an intelligent agent.

**Key Components:**
- `Main.java` - Entry point that wires OpenAiChatModel with tool-use capabilities
- `CalculatorTools` - Example tool decorated with `@Tool` annotation for arithmetic operations
- `Assistant` interface - Service interface defining agent interaction methods

**Pattern:** LLM + Tool Binding via AiServices

---

### 2. **allmessages**
Extends the aichat pattern with multi-turn conversation support and message handling. Demonstrates chat-based interactions with stateful context.

**Status:** Similar architecture to aichat with enhanced messaging capabilities.

---

### 3. **langGraph**
Implements graph-based agent workflows using LangChain4j. Explores state machines and complex agent logic with explicit workflow definition.

**Pattern:** Graph-based control flow for multi-step agent reasoning.

---

### 4. **loader**
A document processing and retrieval system with two main workflows:

#### **Load.java (Document Ingestion Pipeline)**
Reads markdown documents, chunks them for vectorization, embeds them with OpenAI, and generates SQL insert statements for a vector database.

**Spec:** `@doc/specs/spec01.md`

**Flow:**
1. Read markdown from `@doc/info.md`
2. Tokenize with 500-char chunks, 50-char overlap (via DocumentSplitters)
3. Embed all chunks using OpenAiEmbeddingModel (384 dimensions)
4. Generate v7 UUIDs for each chunk
5. Write INSERT SQL to `@doc/ingestion.sql`

**Schema Target:** MariaDB `document_vectors` table (id, content, embedding)

---

#### **Inquiry.java (RAG Query Engine)**
Implements Retrieval-Augmented Generation: embeds a query, finds the 3 most similar documents from the vector database, and uses them as context to answer questions via OpenAI GPT-4o.

**Spec:** `@doc/specs/spec02.md`

**Flow:**
1. Embed query using OpenAiEmbeddingModel (384 dimensions)
2. Search MariaDB for top 3 closest vectors via VEC_DISTANCE_COSINE
3. Retrieve document IDs, content, and distance metrics
4. Build context from retrieved documents
5. Query OpenAI GPT-4o with context + original question
6. Display retrieved documents and final answer

**Database:** MariaDB on localhost:3306 (aitest/aitest)

---

## Technology Stack

- **Language:** Java 23
- **Build:** Maven
- **AI Framework:** LangChain4j (0.35.0)
- **LLM Provider:** OpenAI (GPT-4o, text-embedding-3-small)
- **Vector Database:** MariaDB with native VECTOR type
- **UUID Generation:** uuid-creator (v7 support)
- **JDBC:** MariaDB Java Client

## Project Structure

```
ai/
├── aichat/                 # Basic AI agent framework
├── allmessages/            # Multi-turn chat variant
├── langGraph/              # Graph-based workflows
├── loader/                 # Document processing + RAG
│   ├── src/main/java/com/view/data/
│   │   ├── Load.java       # Spec01: Ingestion pipeline
│   │   └── Inquiry.java    # Spec02: RAG query engine
│   ├── doc/
│   │   ├── info.md         # Source document (The Intelligent Investor)
│   │   ├── db.sql          # Schema definition
│   │   ├── ingestion.sql   # Generated INSERT statements (output)
│   │   └── specs/
│   │       ├── spec01.md   # Ingestion requirements
│   │       └── spec02.md   # RAG query requirements
│   └── pom.xml             # Dependencies
├── CLAUDE.md               # Codebase guidelines
└── README.md               # This file
```

## Quick Start

### Prerequisites
- Java 23+
- Maven
- MariaDB 11+ (running on localhost:3306)
- OpenAI API key

### Setup

1. **Set environment variable:**
   ```bash
   export OPENAI_API_KEY=sk-...
   ```

2. **Create MariaDB database and table:**
   ```bash
   mysql -u aitest -p
   CREATE DATABASE crt_vector;
   USE crt_vector;
   # Run loader/doc/db.sql
   ```

### Running Modules

**aichat (Simple Agent):**
```bash
cd aichat
mvn exec:java -Dexec.mainClass="com.view.data.MainAllMessages"
```

**loader - Load (Document Ingestion):**
```bash
cd loader
mvn exec:java -Dexec.mainClass="com.view.data.Load"
# Generates: loader/doc/ingestion.sql
```

**loader - Inquiry (RAG Query):**
```bash
cd loader
mvn exec:java -Dexec.mainClass="com.view.data.Inquiry"
# Outputs: Retrieved documents + OpenAI answer
```

## Key Design Patterns

1. **LLM + Tool Binding:** Wire models and tools via AiServices (aichat, allmessages)
2. **Embedding Pipeline:** Text → Chunks → Embeddings → Vector Database (Load.java)
3. **RAG (Retrieval-Augmented Generation):** Query → Embed → Search → LLM Context (Inquiry.java)
4. **Parameterized SQL:** Safe vector queries with VEC_FromText and prepared statements

## Notes

- All modules use OpenAI API key from environment; ensure it's set before running
- Vector dimension: 384 (text-embedding-3-small truncated from 1536)
- Distance metric: Cosine similarity (VEC_DISTANCE_COSINE in MariaDB)
- Chunk size: 500 characters with 50-character overlap (configurable in Load.java)
- UUID format: Version 7 (time-based, monotonically increasing)

## Development

Refer to `CLAUDE.md` for extended guidelines on extending the codebase, adding new tools, and dependency management.
