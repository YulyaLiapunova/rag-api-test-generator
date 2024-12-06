# RAG API Test Generator

## Overview

This is a Java-based **Retrieval-Augmented Generation (RAG)** tool designed to automatically generate API tests for changes made in a pull request. The tool uses vector databases **Milvus** for efficient similarity searches and generates test cases based on the relevant code changes.

## Features

- Integrates with **Milvus** to store and query embeddings.
- Uses embeddings to retrieve the most relevant information and auto-generate API tests.
- Supports REST APIs for managing vectors and generating tests.

## Setup

1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/rag-api-test-generator.git
    ```

2. Navigate to the project directory:
    ```bash
    cd rag-api-test-generator
    ```

3. Install dependencies and build the project:
    ```bash
    mvn clean install
    ```

4. Run the application:
    ```bash
    mvn spring-boot:run
    ```

5. Configure Milvus and set up the host in `application.properties`.

## API Endpoints

- **POST /milvus/create**: Create a collection in the vector database.
- **POST /milvus/insert**: Insert vectors into a collection.
- **POST /milvus/query**: Query vectors with a similarity search.

## License

This project is licensed under the MIT License.
