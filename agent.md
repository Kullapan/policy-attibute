# Agent Context: Policy Attribute Management System

## 1. Tech Stack & Environment
- **Backend:** Kotlin 1.9, Spring Boot 3.x (Gradle — `build.gradle`)
  - Plugins: `kotlin-jvm`, `kotlin-spring`, `kotlin-jpa`
  - **No Lombok** — native Kotlin `data class`, primary constructors, and `val`/`var` are used throughout.
  - Testing: JUnit 5 + `mockito-kotlin` (v5.x) for type-safe mocking
- **Frontend:** React 19, TypeScript, Tailwind CSS
- **Database:** PostgreSQL (using JPA/Hibernate, `kotlin-jpa` plugin enables no-arg constructors)
- **Infrastructure:** OpenShift (Target Platform)
- **API Style:** RESTful with JSON. Always provide OpenAPI/Swagger annotations.

## 2. Architecture & Patterns
- **Standard:** Spring MVC — standard 3-tier layered architecture.
- **Package Structure** (under `com.example.kk.policyattribute`):
    - `controller` — `@RestController` classes + `GlobalExceptionHandler` (`@RestControllerAdvice`)
    - `service`    — `@Service` business-logic classes
    - `repository` — `@Repository` Spring Data JPA interfaces
    - `model`      — `@Entity` JPA classes, enums (`AttributeStatus`, `DataType`), and embedded IDs
    - `dto`        — Request/Response data-transfer objects
    - `exception`  — Custom `RuntimeException` subclasses
    - `config`     — `@Configuration` classes (CORS, JPA Auditing)
- **Naming Convention:**
    - Kotlin: camelCase for variables/methods/functions, PascalCase for classes.
    - Database: snake_case for tables and columns.
    - Attribute Codes: ALWAYS use UPPER_SNAKE_CASE (e.g., `MAX_LIMIT`).
    - Prefer `val` over `var` where possible. Use Kotlin `data class` for DTOs and embedded IDs.
- **Data Integrity:**
    - Use Soft Deletes (`status` = 'ARCHIVED') for attributes.
    - Implement Optimistic Locking (`@Version`) for concurrent edits.

## 3. Specific Project Rules
- **Policy Mapping:** All mapping must use `attribute_code` as the primary lookup key, not just a UUID, to ensure human-readability in APIs.
- **Bulk Upload:** Use a streaming approach for large CSV/Excel files to minimize memory footprint on OpenShift.
- **Validation:** Always validate `attribute_value` against the defined `data_type` in the Attribute Dictionary.

## 4. UI/UX Guidelines (from Stitch)
- Follow the design tokens defined in the Stitch MCP bridge.
- Use Toast notifications for CRUD success/failure.
- Ensure all "Delete" actions triggered from the UI prompt for confirmation.

## 5. Development Workflow
- When generating code, prioritize readability and SOLID principles.
- Always include Unit Tests (JUnit 5 for Backend, Vitest/Jest for Frontend).
- If a task involves a new Attribute, check the `Attribute Master` table first.

### Feature: Dynamic Regex Validation
- **Requirement:** Every attribute value must be validated against its `regex_pattern` defined in `attribute_master`.
- **Backend (Kotlin):**
    - In `PolicyAttributeService`, fetch the `regex_pattern` for the given `attribute_code`.
    - Use `java.util.regex.Pattern.compile()` (memoized in `ConcurrentHashMap`) to validate the incoming `attribute_value`.
    - Throw `AttributeValidationException(code, message)` if the match fails, returning the `regex_error_msg` to the client.
- **Frontend (React):** - Create a reusable `DynamicInput` component.
    - It should accept `regex` and `errorMessage` as props.
    - Use `onBlur` or `onChange` to validate input and set the UI error state.
- **Bulk Upload:** - During the validation phase, apply the same regex check to every row in the CSV.
    - Include "Regex Format Mismatch" in the error report for failed rows.