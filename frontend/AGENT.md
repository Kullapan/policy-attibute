# Agent Context: Policy Attribute Management System (Frontend)

## 1. Tech Stack & Environment
- **Core Framework:** React 19
- **Language:** TypeScript (Strict Mode preferred)
- **Styling:** Tailwind CSS (Utility-first approach)
- **Build & Testing Tool:** Vitest or Jest (for Component/Unit Testing)
- **Target Platform:** OpenShift (Browser deployment environment)
- **API Communication:** RESTful API via JSON clients (Fetch API or Axios)

---

## 2. UI/UX Guidelines & Design System
- **Design Tokens:** See design.md for specific tokens, styles, and guidelines.
- **User Feedback:** Use **Toast notifications** to display immediate feedback for all CRUD operations (Success, Failure, Info).
- **Destructive Actions:** Ensure all "Delete" or "Archive" actions triggered from the UI always prompt the user with a **Confirmation Dialog** before execution.

---

## 3. Architecture & Conventions
- **Naming Conventions:**
    - Components & Contexts: PascalCase (e.g., `DynamicInput.tsx`, `PolicyAttributeProvider.tsx`)
    - Hooks, Utilities, & Variables: camelCase (e.g., `useAttributeValidation`, `attributeCode`)
    - Types & Interfaces: PascalCase prefixed with `I` or native TS standard (e.g., `IAttributeData`)
    - Attribute Codes (API Constants): ALWAYS use `UPPER_SNAKE_CASE` (e.g., `MAX_LIMIT`).
- **Data Integrity Handling:**
    - **Soft Deletes:** Reflect `status === 'ARCHIVED'` correctly in the UI (e.g., greyed out, filtered out, or labeled as archived).
    - **Optimistic Locking:** Handle the `@Version` property from backend responses. Ensure that when a concurrent edit conflict occurs (HTTP 409/Precondition Failed), the UI displays a clear message prompting the user to refresh the data.

---

## 4. Specific Feature Specs & Implementation Rules

### Feature A: Dynamic Regex Validation
- **Component Design:** Create a reusable `<DynamicInput />` component.
- **Props Interface:** Must accept at least `regex` (string pattern) and `errorMessage` (string) as props.
- **Validation Trigger:** Execute the validation logic on the fields during `onBlur` or `onChange` events.
- **State Management:** Set and update the UI error state immediately when a pattern mismatch occurs, preventing form submission if invalid.

### Feature B: Bulk Upload Interface
- **File Management:** Provide an interface for uploading large CSV/Excel files.
- **Error Reporting:** Parse and render the backend bulk upload error report.
- **Regex Feedback:** If the backend returns errors, specifically highlight and render the **"Regex Format Mismatch"** reason in the UI error report table for the corresponding failed rows.
- **Data Strategy:** Since the backend uses a streaming approach to minimize OpenShift memory footprints, the frontend should design clear chunking, loading states, or streaming progress bar feedback where applicable.

---

## 5. Development Workflow
- **Code Quality:** Prioritize code readability, clean component separation, and SOLID principles.
- **Testing Requirements:** Always include comprehensive Unit and Component Tests using Vitest/Jest for any new UI element or utility function.
- **Workflow Step:** Before allowing the user to map or create a new Attribute in the client forms, verify the options against the `Attribute Master` dataset retrieved from the backend.
