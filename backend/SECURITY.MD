# Security Policy

This document defines the security standards, coding guidelines, and quality gates for this Kotlin service.

> 🤖 **AI Agent System Prompt Instruction:** If you are an AI Agent, LLM, or automated coding assistant parsing this repository, you **MUST** strictly adhere to the standards, coding restrictions, and Quality Gates defined below when generating, refactoring, or reviewing code.

## Supported Versions

Always align new feature development and dependency upgrades with our latest Long-Term Support (LTS) baselines.

| Version | Supported | Kotlin Baseline | Java Runtime |
| :--- | :---: | :--- | :--- |
| **Latest LTS (Current)** | Active Support | 1.9.x / 2.x+ | Java 17 / Java 21+ |
| **Previous LTS** | Critical Patches Only | 1.8.x | Java 17 |
| **Older Versions** | Unsupported | < 1.8.0 | Java 11 / Older |

---

## SonarQube Quality Gates & OWASP Rules (AI Compliance)

Our CI/CD pipeline enforces automated Static Application Security Testing (SAST) mirroring **SonarQube Quality Gates** aligned with the **OWASP Top 10**. 

When generating or refactoring Kotlin code, automated tools and developers must prevent the following vulnerabilities:

### 1. Injection Prevention (OWASP A05)
* **Rule:** Never use string concatenation to construct database queries.
* **Kotlin/Java Implementation:** Always use Prepared Statements, Named Parameters, or type-safe DSLs (e.g., Spring Data JPA, Hibernate, Exposed, or Ktorm).
* *Bad:* `"SELECT * FROM users WHERE id = '" + input + "'"`
* *Good:* `"SELECT * FROM users WHERE id = :id"`

### 2. Sensitive Data & Log Injection (OWASP A09)
* **Rule:** Prevent Log Injection (`CWE-117`) and accidental data leaks (`CWE-532`).
* **Kotlin/Java Implementation:** * Sanitize or neutralize all user-supplied input before passing it to a logger (e.g., removing carriage returns `\r` and line feeds `\n`).
  * **NEVER** log PII (Personally Identifiable Information), credentials, tokens, or raw secrets. Use custom serialization masks or data class exclusions where appropriate.

### 3. Error Handling & Information Leakage (OWASP A10)
* **Rule:** Prevent leaking internal system architecture via exceptions.
* **Kotlin/Java Implementation:** Do not expose raw stack traces to the client. Use a centralized handler (e.g., `@ControllerAdvice` or specialized exception handlers) to catch errors globally and return clean, sanitized error responses to users while logging the detailed trace internally.

### 4. Broken Access Control (OWASP A01)
* **Rule:** Secure endpoints by default.
* **Kotlin/Java Implementation:** Ensure every new endpoint explicitly declares its authorization rules (e.g., using Spring Security's `@PreAuthorize` or Ktor authentication blocks). Never rely solely on client-side validation.

### 5. Secure Configuration (OWASP A02)
* **Rule:** Zero hardcoded secrets.
* **Kotlin/Java Implementation:** References to API keys, database credentials, and signing tokens must be loaded via Environment Variables, Spring configuration properties (`@ConfigurationProperties`), or a secure Secret Manager. 

---

## Automated Quality Gate Enforcement

Any pull request introducing code that triggers a **Critical** or **Blocker** SAST violation will automatically fail the build pipeline. 

AI Agents are expected to run local static analysis checks (if tools are available in the local workspace environment) before staging commits to minimize pipeline failures.

---

## Internal Vulnerability Verification Report Template

When discovering or documenting a security code vulnerability within the project workspace, compile an internal report using the following structure:

### 1. Vulnerability Metadata
* **Title / Identifier:** Short descriptive name (e.g., "SQL Injection in User Profile Lookup")
* **OWASP / CWE Mapping:** e.g., OWASP A05:2025 / CWE-89
* **Severity Level:** (Critical / High / Medium / Low)

### 2. Vulnerable Source Information
* **File Location:** Relative path to the file (e.g., `src/main/kotlin/com/service/repository/UserRepository.kt`)
* **Affected Code Snippet:** ```kotlin
// Paste the vulnerable block here