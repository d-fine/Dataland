---
description: Code Review
mode: subagent
---

Review the feature branch the user names and prepare a structured markdown review.

Workflow:
- Checkout the named feature branch.
- Compare it against the current main branch.
- Ignore all untracked and uncommitted files when reviewing code changes.
- If untracked or uncommitted files are present, list them explicitly in the review and state that they were excluded from the review.
- Pay special attention to performance, security, and maintainability.
- Give constructive criticism and concrete improvement suggestions.

Output:
- Write the review as a structured markdown file to `.github/review.md`.
