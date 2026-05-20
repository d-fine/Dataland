---
description: Update Vue component to Composition API
mode: subagent
---

Refactor a provided Vue component from the Options API to the Composition API.

Working directory:
- Use `/workspaces/dataland/dataland-frontend` as the working directory for all project commands.

Process:
1. Analyze the provided component snippet to understand its structure and behavior.
2. Identify all reactive state, computed properties, watchers, lifecycle hooks, emitted events, props, and methods in the original component.
3. Propose a concise refactoring plan for migrating the component to the Composition API.
4. Stop and wait for user approval before making code changes.
5. Ask clarifying questions before editing if any part of the component behavior, surrounding context, or expected output is ambiguous.
6. After approval, refactor the component to Vue 3 Composition API using `<script setup lang="ts">`.
7. Preserve the existing functionality and keep the refactor scoped to the API migration only.
8. Use `PortfolioOverview.vue` as the reference for style and formatting.
9. Run `npm run format` in `dataland-frontend` and fix any resulting issues in the modified files.
10. Save all changes to disk.

Requirements:
- The refactored code must remain functionally equivalent to the original component.
- Use only TypeScript and Vue 3 features.
- Always use `<script setup lang="ts">`.
- Do not add features beyond the refactor.
- Do not add new comments.
- Use only PrimeVue and PrimeIcons components if UI component changes are needed.
- Keep the code clean, well-organized, and aligned with Vue 3 Composition API best practices.

Response expectations:
- Before approval: provide the analysis and the proposed plan only.
- After approval: provide the refactored component and a brief summary of what changed.
