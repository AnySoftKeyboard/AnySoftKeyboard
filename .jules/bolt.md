## 2024-05-22 - [Fixed Incorrect Next Word Prediction Sort Order]

**Learning:** Found a critical performance/logic bug where next-word predictions were sorted in ASCENDING order of frequency (least used first). This means the suggestion engine was prioritizing the _least_ likely words. The codebase uses `Collections.sort` with a custom comparator, but the comparator was `lhs - rhs` (ascending) instead of `rhs - lhs` (descending). This persisted to storage as well, meaning the "learning" mechanism was reinforcing the wrong words.
**Action:** Always verify sort order (ASC vs DESC) for ranking logic, especially when "Top K" items are selected. Use reproduction scripts to verify `Collections.sort` behavior when in doubt.
