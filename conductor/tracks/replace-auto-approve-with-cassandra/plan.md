# Implementation Plan: Replace Auto-Approve with Cassandra

## Phase 1: Research and Preparation
- [x] Research current auto-approve/code-review code.
- [x] Research Cassandra usage and latest SHA.

## Phase 2: Code Removal
- [ ] Remove `js/auto_approval/` directory and all its files.
- [ ] Remove `js/ai/ai_code_review.ts` and `js/ai/mcp_read_multiple_files.ts`.
- [ ] Modify `js/ai/index.ts` to remove the `codeReview` command and `AiCodeReviewer` import/usage.
- [ ] Update `js/ai/BUILD.bazel` to remove targets for `ai_core_review` and `mcps`, and update `ai_cli` dependencies.

## Phase 3: Workflow Update
- [ ] Update `.github/workflows/auto_approve.yml` to use `menny/cassandra`.
- [ ] Configure `fetch-depth: 0` in `actions/checkout`.
- [ ] Set up `provider`, `model_id`, `provider_api_key`, `base`, `head`, and `reviewer_github_token` inputs for Cassandra.

## Phase 4: Validation
- [ ] Verify that the Bazel build still works for remaining tools (`js/ai`'s `translationsVerification`).
- [ ] (Optional) Trigger the workflow manually if possible, or verify its syntax.
