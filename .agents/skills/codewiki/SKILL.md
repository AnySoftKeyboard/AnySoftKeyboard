---
name: codewiki
description: Scout large codebases via Google CodeWiki (structure/read/ask). Use first for orientation on AnySoftKeyboard or public external deps before local exploration.
user-invocable: true
---

# CodeWiki scouting

```bash
codewiki structure <owner>/<repo>          # section titles only, start here
codewiki read <owner>/<repo>               # full wiki as Markdown
codewiki ask <owner>/<repo> "<question>"   # single targeted question, ~10 s
```

- If `codewiki` is missing, skip this skill and explore locally with `Glob`/`Grep`/`Read`.
- Public repos only. Run with `CODEWIKI_TLS_VERIFY=1` and `CODEWIKI_CACHE_DIR="$PWD/scratch/.codewiki-cache"`.
- `structure` first, then read 2–3 matching local files. The wiki snapshot may lag `main`; verify against local files before editing.
- `read`/`ask` only when local search stalls.
