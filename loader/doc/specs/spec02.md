- module @loader
- in @doc/db.sql is the schema associated with a table

- change in Inquiry.java only
- you connect to database mariadb on localhost port 3306 with aitest / aitest credentials
- there is a table document_vectors as defined in @db.sql

- get inquiry
- using open ai model, vectorize it
- query the db for the first three entries with the smallest distance to the query
- put together the request and query open ai
- show result

for now query is "what kind of emotional discipline is needed for an intelligent investor"

the open api key is already in the environment or I will copy it there