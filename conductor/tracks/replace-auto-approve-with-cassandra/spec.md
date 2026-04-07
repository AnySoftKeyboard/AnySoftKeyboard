# Specification: Replace Auto-Approve with Cassandra

## Goal
Replace the current custom AI code review and auto-approval logic in `AnySoftKeyboard` with the `Cassandra` tool from `https://github.com/menny/cassandra/`.

## Requirements
1.  Remove all code related to `auto-approve` and `code-review` from the repository.
2.  Specifically remove `js/auto_approval/` and its references.
3.  Specifically remove the `codeReview` command and `AiCodeReviewer` from `js/ai/` and its references.
4.  Update `.github/workflows/auto_approve.yml` to use `menny/cassandra` at SHA `c31e5c67624ca33ee7094b567e4d14a96639fe8b`.
5.  Ensure the new workflow is correctly configured with `fetch-depth: 0` and necessary permissions.
