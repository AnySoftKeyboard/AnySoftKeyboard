# Implementation Plan: Replace Translation Verification with Cassandra

## Phase 1: Preparation

- [x] Research existing translation verification prompt.
- [x] Research Cassandra custom guidelines usage.
- [x] Create `.cassandra/translation_rules.md` with the extracted prompt.

## Phase 2: Script Update

- [x] Modify `scripts/ci/localization_update_summary.sh` to use `cassandra` with `--main-guidelines .cassandra/translation_rules.md`.
  - _Update:_ Actually moved the Cassandra call to the GitHub Workflow for better integration.

## Phase 3: Cleanup

- [x] Remove `js/ai/translation_verification.ts` and `js/ai/index.ts`.
- [x] Remove `js/ai/BUILD.bazel`.
- [x] Update `conductor` plans.

## Phase 4: Validation

- [x] Run `bazel test //js/...` to ensure everything else still works.
