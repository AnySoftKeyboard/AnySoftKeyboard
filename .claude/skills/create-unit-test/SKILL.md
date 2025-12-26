---
name: create-unit-test
description: Create and run unit tests following the project's architecture and guidelines (Robolectric, naming, location).
---

# Create Unit Test

This skill guides the creation of unit tests to ensuring consistency and correctness.

## Workflow

1.  **Analyze and Plan**:
    - **Goal**: Understand what needs testing.
    - **Action**: Examine the source code.
    - **Action**: Identify "happy path" cases (core functionality).
    - **Action**: Identify "error cases" and "edge cases" based on likelihood.
    - **Action**: Do **not** suggest implementation details unless asked; focus on behavior.

2.  **Setup Test File**:
    - **Naming**: `[OriginalClassName]Test.kt`.
    - **Location**: `[module]/src/test/java/[package/path]/`.
    - **Runner**: If testing Android components, you **MUST** use Robolectric.
    - **Mocking**: Prefer creating **Fakes** over Mocks or patches. Use Mocks only if Fakes are too complex.

3.  **Implement Tests**:
    - Write clear, descriptive test methods.
    - Follow the "Arrange-Act-Assert" pattern.
    - Ensure all resources (strings, themes) are properly mocked or provided via Robolectric.

4.  **Run Tests**:
    - **Command**: `./gradlew :[module]:testDebugUnitTest`
    - **Example**: `./gradlew :database:testDebugUnitTest`
    - **Tip**: Before any commit, run `./gradlew testDebugUnitTest` to ensure everything passes.

5.  **Handle Failures**:
    - **Goal**: Diagnose and fix issues.
    - **Action**: If a test fails, locate the full error report.
    - **Path**: `[module]/build/test-results/testDebugUnitTest/TEST-[package.name.TestClassName].xml`.
    - **Example**: `app/build/test-results/testDebugUnitTest/TEST-com.anysoftkeyboard.janus.app.MainActivityTest.xml`.

## Guidelines

- **Consistency**: Always place tests in the `test` source set, not `androidTest` (unless specifically writing instrumentation tests, which is rare for this skill).
- **Reliability**: Avoid flaky tests. Ensure deterministic behavior.
