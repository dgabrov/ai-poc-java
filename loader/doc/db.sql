CREATE TABLE document_vectors (
  id VARCHAR(36) PRIMARY KEY,
  content TEXT NOT NULL,
  embedding VECTOR(384) not null
);

