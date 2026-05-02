# Code Reuse and Refactoring Guidelines

- **Detect Duplication**: Actively look for logic within the diff or surrounding context that is repeated or highly similar.
- **Extract Functions**: If logic is used multiple times within the same file, suggest extracting it into a well-named private function to improve readability and maintainability.
- **Utilize Common Utilities**: Be aware of project-wide utility patterns. If a custom implementation can be replaced by a standard language feature or an existing project-wide utility, strongly suggest the more idiomatic approach.
- **Promote Composability**: Favor small, single-purpose functions that can be easily tested and reused over large, monolithic blocks of code.
