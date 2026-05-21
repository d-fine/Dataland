# Framework Toolbox

- This module contains framework generation, consistency, and integration tooling.
- It is not a typical backend service: besides unit tests, it has custom execution tasks that CI runs directly.
- Many tasks assume the repository root as working directory and read framework inputs from `dataland-framework-toolbox/inputs/`.

# Commands

- Run unit tests: `./gradlew :dataland-framework-toolbox:test`
- Run integration tests: `./gradlew :dataland-framework-toolbox:integrationTest`
- Generate the framework list used by CI: `./gradlew :dataland-framework-toolbox:runCreateFrameworkList`
- Run consistency generation for a specific framework: `./gradlew :dataland-framework-toolbox:runCoverage --args='<framework>'`

# Verification

- For normal Kotlin changes, run `./gradlew :dataland-framework-toolbox:test`.
- If you change integration behavior, framework loading, or input handling, run `./gradlew :dataland-framework-toolbox:integrationTest`.
- If you change framework generation logic, framework definitions, or toolbox outputs, run `./gradlew :dataland-framework-toolbox:runCreateFrameworkList` and the relevant `runCoverage` command.
- After generator-style runs, check for unintended tracked-file changes, matching CI behavior.

# Package-Specific Rules

- Be careful with changes that read or transform files under `dataland-framework-toolbox/inputs/`; they affect framework generation behavior directly.
- Prefer intentional output changes only. If a toolbox run changes tracked files, verify that the diff is expected before keeping it.
- Keep module-specific guidance here focused on the custom toolbox tasks; the root `AGENTS.md` still governs shared Kotlin, Gradle, and monorepo rules.
