# Website Package

- This package contains the Astro-based static website.
- It also consumes Vue components and shared code from `dataland-sharedElements`.
- Preserve the existing Astro and Vue integration patterns when editing pages or shared content rendering.

# Commands

- Install dependencies: `npm install`
- Start local dev server: `npm run dev`
- Format files: `npm run format`
- Check formatting: `npm run formatci`
- Lint files: `npm run lint`
- Check lint without fixes: `npm run lintci`
- Type-check: `npm run typecheck`
- Build production site: `npm run build`

# Verification

- For most changes, run:
  - `npm run lintci`
  - `npm run formatci`
  - `npm run typecheck`
- Run `npm run build` when changing rendered pages, markdown/content pipelines, route structure, or Vue components used by the site.
- If you change shared elements consumed by the website, also verify `dataland-sharedElements` and any affected consuming package.

# Package-Specific Rules

- Keep content and generated output understandable for non-technical readers when working on public-facing pages.
- Prefer existing content structure and component conventions over introducing new layout systems.
- Avoid package-local rules that conflict with the root `AGENTS.md`; the root file still governs shared monorepo workflows.
