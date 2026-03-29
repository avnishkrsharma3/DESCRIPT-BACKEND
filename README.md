# descriptAI Backend

A Spring Boot backend service that fetches product data, stores it in MongoDB, and generates AI-assisted product descriptions through pluggable providers (currently Groq and Gemini wiring, with Groq being the working end-to-end path).

## Why this project exists

`descriptAI-backend` is designed to support a product-content workflow:
1. Import product catalog data from an external products API.
2. Persist searchable product records in MongoDB.
3. Generate AI descriptions for selected products.
4. Save only user-approved AI output as a separate approved record set.

## Tech stack

- Java 17
- Spring Boot 3.5.x
- Spring Web + Validation + Actuator
- Spring Data MongoDB
- Lombok
- Google GenAI SDK (Gemini client wiring)
- Maven

## High-level architecture

- **Controllers** expose REST APIs for product operations and AI generation.
- **Services** encapsulate business logic (search, approval persistence, prompt construction, response mapping).
- **AIClient interface** allows provider selection by configuration (`ai.provider`).
- **Repositories** persist raw products and approved AI descriptions.

## Key modules

- `ProductsController`
  - Fetch all/search products from DB.
  - Fetch product catalog from external source and store in DB.
  - Save approved AI-generated content.
- `AIGenerationController`
  - Batch AI description generation by product IDs and prompt preferences.
- `GroqApiClient` + `GroqAIService`
  - Main operational pipeline for prompt building, API invocation, response parsing, and DTO shaping.
- `GeminiApiClient`
  - Initialized client and single-product helper method present, but batch interface method currently returns an empty list.

## API summary

Base URL: `http://localhost:8081`

### Product APIs

- `GET /api/products` — list all products in MongoDB.
- `GET /api/products/search?q=<query>` — search products.
- `POST /api/products/fetch` — pull products from configured external API and save them.
- `POST /api/products/save` — persist a user-approved AI description payload.

### AI API

- `POST /api/AI/generate` — generate descriptions for input product IDs based on prompt style.

#### Example request

```json
{
  "productIds": ["1", "2", "3"],
  "prompts": "professional,short,seo"
}
```

## Configuration

Environment-backed properties are defined in `application.properties`.

Required environment variables:

- `MONGODB_URI`
- `EXTERNAL_API_URL`
- `AI_CLIENT` (`groq` or `gemini` bean names)
- `GEMINI_API_KEY`
- `GEMINI_API_URL`
- `GROQ_API_KEY`
- `GROQ_API_URL`

> Tip: for current end-to-end generation behavior, use `AI_CLIENT=groq`.

## Run locally

```bash
./mvnw spring-boot:run
```

App default port is `8081`.

## Test

```bash
./mvnw test
```

## Deployment

The repo includes:
- `Dockerfile`
- `render.yaml`

These provide a base path for containerized and Render-style deployment.

## Known limitations / improvement areas

- Gemini client does not yet implement the batch `AIClient` interface method.
- Some error handling is generic (`500` with minimal payload details).
- Prompt-format assumptions are strict; resilient parsing/validation can be improved.
- API docs (OpenAPI/Swagger) are not yet included.

## Suggested next steps

1. Implement `GeminiApiClient.generateProductDescription(List<String>, String)` to match Groq path.
2. Add integration tests for `/api/products/fetch` and `/api/AI/generate`.
3. Add OpenAPI docs and request/response examples.
4. Introduce centralized exception handling (`@ControllerAdvice`) and consistent error schema.
