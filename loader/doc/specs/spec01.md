- module @loader
- in @doc/db.sql is the schema associated with a table
- you got info.md under @doc

Create functionality in Main.java that does as well
- reads the info.md file
- tokenizes it with 500 / 50 to prepare the chunks for vectorization
- use OpenAiEmbeddingModel to calculate the vectors for all the chunks
- generate insert sql script in @doc/ingestion.sql that would insert rows in the table document_vectors
- in id please put a v7 uuid
- in content you put the chunk
- in embedding you put the vector

- add whatever you need in pom.xml
- I will add the open AI key in the environment
