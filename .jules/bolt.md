## 2024-05-22 - [Fixed Incorrect Next Word Prediction Sort Order]

**Learning:** Found a critical performance/logic bug where next-word predictions were sorted in ASCENDING order of frequency (least used first). This means the suggestion engine was prioritizing the _least_ likely words. The codebase uses `Collections.sort` with a custom comparator, but the comparator was `lhs - rhs` (ascending) instead of `rhs - lhs` (descending). This persisted to storage as well, meaning the "learning" mechanism was reinforcing the wrong words.
**Action:** Always verify sort order (ASC vs DESC) for ranking logic, especially when "Top K" items are selected. Verify with unit-tests.

## 2025-02-23 - Android SDK Missing in Environment

**Learning:** The sandbox environment lacks Android SDK, making Gradle tasks for Android modules fail. Verification must rely on Bazel for JS/Java or CI.
**Action:** Check for Android SDK before attempting Gradle builds. Prefer non-Android optimizations if SDK is missing.
