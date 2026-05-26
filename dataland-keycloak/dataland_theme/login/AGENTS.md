# Keycloak Theme Package

- This package contains the Keycloak login theme frontend.
- It is separate from the main Vue application and has its own npm-based lint/build workflow.
- Preserve the established theme structure and asset usage unless the change specifically requires broader theme work.

# Commands

- Install dependencies: `npm install`
- Build the theme: `npm run build`
- Lint with fixes: `npm run lint`
- Lint in CI mode: `npm run lintci`

# Verification

- For most changes, run `npm run lint` while editing.
- Before finishing, run `npm run lintci`.
- Run `npm run build` when changing theme templates, styles, or assets that affect the generated theme output.

# Package-Specific Rules

- Reuse the existing theme stack and assets before adding new packages or patterns.
- Keep changes scoped to the Keycloak theme; do not mirror main-frontend conventions that do not apply here.
- If a UI change overlaps with shared branding assets or typography, verify that the theme still builds cleanly.
