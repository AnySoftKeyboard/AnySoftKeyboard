---
description: 
globs: 
alwaysApply: true
---
# Building
- this project uses Bazel build system for TypeScript and some Java code.
- this project uses Gradle build system for Android related code (mostly under the forlder `/ime`, `/api` and `/addons`).
- Prefer not adding new 3rd party dependencies.
- Do not add 3rd-party dependencies without approval. If you have a solution that requires a new 3rd party, ask for an approval before implementing this solution.
  
# Lint and Format
- don't try to fix linting or formatting issues, we have auto-fixers for that. This is applicable for *all* code in the codebase.
- You can run the auto-fixers with `bazel run //:format`. This is applicable for *all* code in the codebase.
- for Android code, you should also run `./gradle spotlessApply`, which had additional formating and linting.

# Git Commit Guidelines
Before creating a commit always run `bazel run //:format`.
If the changed code is related to Android, also run `./gradle spotlessApply`.

## Commit Message
When creating a commit message, follow these guidelines:
- **Title:** Use a concise title. Prefix the title with `[Gemini-cli]`.
- **Description:** The description should include a short description of the issue (bug, feature-request, crash, chore, etc) and a short description of the solution.

# Tests
- when ask to suggest tests for a function or file:
  - Do not implement anything or suggest how to implement.
  - You should only look at the code and suggest tests based on functionality and error cases.
  - Identify the "happy path" - core functionality - cases and mark them as such in your suggestions
  - Identify the error cases and mark them as such in your suggestions. Estimate importance based on likelyhood.
  - Identify the edge cases and mark them as such in your suggestions. Estimate importance based on likelyhood.
- when implementing tests:
  - For TypeScript, the test file name follows the pattern `[original_file_name].test.ts`
  - For Java and Kotlin, the test file name follows the pattern `[original_file_name]Test.java`
  - For typescript, use `node` test runner. Look at other test files for inspiration. 
  - prefer creating fakes over mocks or patches. But, if it is simpler to patch or mock, do that.
  - it is not very important to type mocks/spies/patch. You can use `any`. In such cases, ignore the typing with `// @ts-expect-error any in tests is fine`
 
## Running Tests
- To run tests in bazel use `bazel test //...`
- To run tests in gradle use `./gradlew :[path]:[to]:[module]:testDebugUnitTest`. For example, to run test under `ime/base-rx`, call `./gradlew :ime:base-rx:testDebugUnitTest`.
- When running tests in the module `:ime:app`, ensure you are using the test filter `--tests` and filter for the desired test class. For example, to run tests for `KeyPreviewsManagerTest` in `:ime:app`, use `./gradlew :ime:app:testDebugUnitTest --tests "com.anysoftkeyboard.keyboards.views.preview.KeyPreviewsManagerTest"`.

# Naming
- use inclusive language when creating variables, functions, class names, stubs, etc:
  - Do not use "dummy", instead use "fake", "mock", "noop" etc.
  - Do not use "blacklist", instead use "disallow-list"
  - Do not use "whilelist", instead use "allow-list"
  - Stay away from: "master", "slave", "insane", "dumb", etc.
  - Use gender neutral pronouns
