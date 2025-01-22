# RAG API Test Generator

## About
This is a Java-based **Retrieval-Augmented Generation (RAG)** tool designed to automatically generate API tests for changes made in a pull request. The tool uses vector databases **Milvus** for efficient similarity searches and generates test cases based on the relevant code changes.

The project is completed during the preparation of Ulia R. Lyapunova's bachelor thesis at SPbPU Institute of Computer Science and Cybersecurity (SPbPU ICSC).

## Features

- Integrates with **Milvus** to store and query embeddings.
- Uses embeddings to retrieve the most relevant information and auto-generate API tests.
- Supports REST APIs for managing vectors and generating tests.

## Authors and Contributors
Advisor and minor contributor: Vladimir A. Parkhomenko
Senior Lecturer at SPbPU ICSC

Main Contributor: Ulia R. Lyapunova
Student at SPbPU ICSC

## Reference (to be updated after publication):
Please, using this repository, cite the following paper
About automatization of API-tests generation with the use of LLM, 2025

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

## License

This project is licensed under the MIT License.
