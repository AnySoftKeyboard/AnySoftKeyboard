---
name: codewiki
description: Scout large codebases via Google CodeWiki (structure/read/ask). Use first for orientation on AnySoftKeyboard or public external deps before local exploration.
user-invocable: true
---

# CodeWiki scouting

Based on upstream `aeroxy/codewiki-cli` skill (`skill/codewiki/SKILL.md`, MIT).
Binary is NOT vendored; install on demand or fall back to local exploration.

## Prerequisite

```bash
codewiki structure <owner>/<repo>          # section titles only (~2 KB)
codewiki read <owner>/<repo>               # full wiki as Markdown (~200+ KB)
codewiki ask <owner>/<repo> "<question>"   # Gemini Q&A, single-turn, ~10 s
```

If `codewiki` is not installed (`brew install aeroxy/tap/codewiki-cli` or
`cargo install codewiki-cli`), skip this skill and use local `Glob`/`Grep`/`Read`.
Public GitHub repos only. Run with:

```bash
export CODEWIKI_TLS_VERIFY=1
export CODEWIKI_CACHE_DIR="$PWD/scratch/.codewiki-cache"
```

(`CODEWIKI_TLS_VERIFY` restores strict cert checking; upstream disables it by
default. `scratch/` is gitignored.)

## Workflow (scouting first)

1. **`structure` first.** For `AnySoftKeyboard/AnySoftKeyboard` this is
   58 lines / ~2.4 KB / ~600 tokens — ~1% of full `read`. Use it to map the
   area, then resolve to 2–3 local files with `Glob`/`Grep` and read only those.
2. **Local checkout is truth.** The CodeWiki snapshot lags `main` (e.g. pinned
   commit `64678c0d` was absent from local history at time of writing). Never
   edit from wiki text; always verify claims against local files.
3. **`read`/`ask` only on stall.** Full `read` (~228 KB here) or a single
   targeted `ask` helps for external deps or a second opinion when local
   search stalls. `ask` is single-turn — one question per invocation.

## When to use

- Orienting in the big `ime/` tree without burning context on broad exploration.
- Researching a public external dep (build tool, library) without a browser.
- Getting an architecture overview or diagram pointers before diving into files.

## When NOT to use

- `codewiki` binary missing — just explore locally.
- Private repos (unsupported upstream).
- As a substitute for reading the files you will change.
