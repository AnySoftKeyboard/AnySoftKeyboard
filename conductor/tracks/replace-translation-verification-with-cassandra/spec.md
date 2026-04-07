# Specification: Replace Translation Verification with Cassandra

## Goal

Replace the custom translation verification logic in `js/ai/translation_verification.ts` with a custom Cassandra guideline set.

## Requirements

1.  Extract the existing translation review prompt into a new guideline file: `.cassandra/translation_rules.md`.
2.  Update `scripts/ci/localization_update_summary.sh` to use Cassandra instead of `bazel run //js/ai`.
3.  Remove all remaining code from `js/ai/` as it will no longer be needed.
4.  Ensure `scripts/ci/localization_update_summary.sh` correctly passes the diff (or the XML report) to Cassandra.
    - _Note:_ Cassandra usually expects a git diff. If the `translation_verification.ts` expected XML, we may need to adapt Cassandra or the way we call it to handle the XML file.
    - _Correction:_ If Cassandra reviews PRs/diffs, we can just point it to the changed localization files with the custom guidelines.

## Verification

- Verify the Cassandra call syntax for custom guidelines.
- Ensure the Bazel build for `js/ai` is no longer needed and can be removed.
