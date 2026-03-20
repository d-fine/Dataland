# Homepage Rework V2 — Implementation Plan

**Date:** 2026-03-18
**Spec:** `dataland-frontend/HOMEPAGE_REWORK_SPEC.md`
**Branch:** `feature/rework-about-page`

---

## Lessons Learned from Previous Implementation

The previous plan had **7 phases, 7 parallel worktrees, and 3 review agents**. Problems:

- **CI failures** — isolated worktree builds compiled individually but broke when merged
- **Over-fragmentation** — too many small agents missing shared dependencies
- **Branch proliferation** — worktrees created their own branches, making merges error-prone

This plan uses **4 phases, no worktrees, all work on one branch** (`feature/rework-about-page`). CI verification happens once at the end.

---

## CI Verification Reference

CI is checked **once** — in Phase 4 after all building and review fixes are done.

### Local CI Gate (mirrors GitHub Actions)

```bash
cd dataland-frontend
npm run typecheck          # vue-tsc --noEmit
npm run lintci             # ESLint — fails on ANY warning (stricter than `npm run lint`)
npm run formatci           # Prettier formatting check
npm run checkdependencies  # Unused/missing deps
npm run testcomponent      # Cypress component tests
```

### Checking Remote CI After Push

```bash
gh run list --branch feature/rework-about-page --limit 3
gh run view <run-id> --log-failed   # View failed job logs
```

**Key CI jobs that must pass:**

| Job | What it checks |
|-----|---------------|
| `gradle-based-tests` | ESLint (`lintci`), Prettier (`formatci`), TypeScript (`typecheck`), Cypress compilation, dependency check |
| `frontend-component-tests` | `npm run testcomponent` (30-min timeout) |
| `e2e-tests` (groups 1-4) | Full Cypress E2E in Docker stack |

### What Breaks CI Most Often

1. **ESLint warnings** — `lintci` fails on ANY warning (stricter than `lint`)
2. **Unused imports/variables** — ESLint catches these as errors
3. **Component test failures** — tests reference old component names/selectors after refactoring
4. **TypeScript errors** — missing imports, wrong types after component renames

---

## Agent Roles

| Role | Agent Type | Identity | Rule |
|------|-----------|----------|------|
| **Builder** | `frontend-developer` | Agent X | Implements all code. |
| **Code Reviewer** | `frontend-developer` | Agent Y | Fresh agent, independent from Agent X. Reads spec + finished code. Never saw the build. |
| **Copy Reviewer** | `copywriter` | — | Verifies every rendered string against the spec. |

**Critical rules:**
- All agents work on the **same branch** (`feature/rework-about-page`). No worktrees, no feature branches.
- Agent Y (code reviewer) must be a **separate invocation** from Agent X (builder).
- Review agents (Phase 3) are **read-only** — they read code and produce issue lists but do not edit files.

---

## Phase 1: Infrastructure + Content + Shared Components

**Agent:** 1 `frontend-developer` (Agent X), serial

### Tasks

1. **SCSS breakpoints** — Create `src/assets/scss/breakpoints.scss` with `$bp-sm: 640px`, `$bp-md: 768px`, `$bp-lg: 1024px`, `$bp-xl: 1440px`. Update `vite.config.ts` to inject via `additionalData`.

2. **useBreakpoint composable** — Create `src/composables/useBreakpoint.ts` per spec section 2.2.

3. **Reduced motion support** — Add global `@media (prefers-reduced-motion: reduce)` rule per spec section 2.6.

4. **Content data files:**
   - Create `src/components/resources/landingPage/landingContent.ts` — testimonials (12 quotes), news items (9 entries with LinkedIn URLs), customer story summaries (3 cards), sector tiles (11 entries)
   - Create `src/components/resources/productPage/productContent.ts` — use cases (7), feature cards (6), pricing data, documentation links (9 URLs), detailed customer stories (MEAG, NORD/LB, ÖVB)
   - Modify `src/components/resources/aboutPage/aboutContent.ts` — update team members (3), update partners (FACT, ISS), update company text

5. **Shared generic components:**
   - Create `src/components/generics/ProblemSolutionBlock.vue` — reusable 3-column problem→arrow→solution layout used by TheWhyUs, ProductHowItWorks, ProductGettingData
   - Create `src/components/generics/AccessibleCarousel.vue` — reusable carousel with auto-scroll, pause button, keyboard nav, `aria-live`, `prefers-reduced-motion` support
   - Create `src/components/generics/NewsCard.vue` — reusable news card (image, title, date, link)

6. **Route registration** — Update `src/router/index.ts` to add routes for `/product`, `/newsletter`, `/contact`, `/testimonials`. Create empty page shells for now (just `<main id="main-content"><p>TODO</p></main>`).

---

## Phase 2: Build All Pages + Navigation + Footer + Cleanup + Tests

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Phase 1 complete.

This is the main build phase. One agent builds everything sequentially on the same branch, in this order:

### 2.1 Landing Page (spec sections 3.1–3.11)

**Create section components** in `src/components/resources/landingPage/`:

1. `TheIntro.vue` — MODIFY existing. Two-column hero (60/40 grid), headline + subtext + 2 CTAs left, illustration right. Remove old search bar from hero (it moves to TheFindLei). Responsive stacking below `$bp-md`.
2. `TheFindLei.vue` — CREATE. Company search section reusing `CompaniesOnlySearchBar`. Centered, max-width 520px.
3. `TheWhyUs.vue` — CREATE. 4 problem-solution pairs using `ProblemSolutionBlock`. 3 CTA buttons. Replaces TheStruggle.
4. `TheTrustedBy.vue` — CREATE. Logo carousel using `AccessibleCarousel`. 14+ logos with pause control.
5. `TheCustomerStories.vue` — CREATE. 3 story cards (MEAG, NORD/LB, ÖVB) in 3-column grid.
6. `TheTestimonials.vue` — CREATE. Quote carousel using `AccessibleCarousel`. 12 cards. CTA → `/testimonials`.
7. `TheFrameworks.vue` — CREATE (replaces old). 6 framework cards in 3x2 grid with orange corner accent.
8. `TheCustomerProfiles.vue` — CREATE. 11 sector tiles in CSS Grid mosaic.
9. `TheNewsInsights.vue` — CREATE. News slider using `AccessibleCarousel` + `NewsCard`. 9 items.

**Update `LandingPage.vue`** — new section order per spec 3.2, update imports, add `id="main-content"`.

### 2.2 Product Page (spec sections 4.1–4.11)

**Create section components** in `src/components/resources/productPage/`:

1. `ProductIntro.vue` — Centered headline, 32px bold, max-width 900px.
2. `ProductHowItWorks.vue` — 3 problem-solution blocks using `ProblemSolutionBlock`. Anchor: `#how-it-works`.
3. `ProductGettingData.vue` — 2 problem-solution blocks. Anchor: `#getting-data`.
4. `ProductFeatures.vue` — 6 feature cards in 3x2 grid. Anchor: `#features`.
5. `ProductUseCases.vue` — 7 use case blocks, alternating layout. Anchor: `#use-cases`.
6. `ProductCustomerStories.vue` — 3 detailed stories (MEAG, NORD/LB, ÖVB). Anchor: `#customer-stories`.
7. `ProductMembershipPricing.vue` — Value props + pricing card + credits visual. Anchor: `#membership-pricing`.
8. `ProductDocumentation.vue` — 9 doc links + 2 CTAs. Anchor: `#documentation`.

**Replace empty shell `ProductPage.vue`** with full template per spec 4.3, all imports, `ContactInquiryModal`.

### 2.3 About Page + Additional Pages (spec sections 5.1–5.8, section 6)

**About Page — create/modify** in `src/components/resources/aboutPage/`:

1. `TheAboutCompany.vue` — CREATE (replaces TheAboutHero). Two-column: logos left (40%), text right (60%). Anchor: `#company`.
2. `TheAboutTeam.vue` — MODIFY. 3 team members with email/LinkedIn. Dark blue background. Anchor: `#team`.
3. `TheAboutPartners.vue` — MODIFY. 2 partners (FACT, ISS) with logos and links. Anchor: `#partners`.
4. `TheAboutUpdates.vue` — CREATE. 3x3 static grid of `NewsCard` components. Anchor: `#updates`.
5. `TheAboutContact.vue` — CREATE. Dark background. Two-column: contact info + static demo form. Anchor: `#contact`.

**Update `AboutPage.vue`** — new section order per spec 5.3, add `ContactInquiryModal`, `id="main-content"`.

**Additional Pages** in `src/components/pages/`:

1. `NewsletterPage.vue` — Dark background, two-column, static form. Route: `/newsletter`.
2. `ContactPage.vue` — Reuses `TheAboutContact`. Route: `/contact`.
3. `TestimonialsPage.vue` — 3x4 grid of 12 testimonial cards with video placeholder. Route: `/testimonials`.

### 2.4 Navigation + Footer (spec sections 2.3–2.5)

**Header** (`LandingPageHeader.vue` — MODIFY):
- Skip-to-content link as first child of `<header>`
- Product/About click-triggered dropdown menus with `role="menu"`, keyboard nav, `aria-expanded`
- Mobile (< $bp-lg): hamburger → slide-down overlay with nav + auth
- CTA labels: "Login" text link + "Try it free" primary button

**Footer** (`LandingPageFooter.vue` — MODIFY or CREATE):
- 4-column top: Dataland (desc + logos), Product (7 links), Company (5 links), Resources (3 links)
- Bottom bar: Legal, Imprint, Data Privacy, Cookie Settings, copyright, LinkedIn
- Responsive: 4-col → 2x2 → 1-col

### 2.5 Cleanup + Test Fixes

**Delete old components** (verify no imports remain before deleting):
- Landing: `TheQuotes.vue`, `TheHowItWorks.vue`, `TheJoinCampaign.vue`, `TheStruggle.vue`
- About: `TheAboutSponsors.vue`, `TheAboutHero.vue`, `TheAboutTrustPillars.vue`, `TheAboutPrinciples.vue`, `TheAboutEcosystem.vue`, `TheAboutBottomCTA.vue`

**Fix component tests:**
- Update `tests/component/components/pages/LandingPage.cy.ts` for new section structure
- Update `tests/component/components/resources/aboutPage/PersonCard.cy.ts` if PersonCard changed
- Run `npm run testcomponent` and fix all failures

---

## Phase 3: Independent Reviews (2 agents in parallel)

**Dependencies:** Phase 2 complete.
**Both agents are read-only** — they read code and produce issue lists. No file edits, no branch interference.

Launch both in a **single message**.

### 3a — `frontend-developer` (Agent Y) — Code Review

**Prompt:**

> You are reviewing code that someone else wrote. You have never seen this code before. Read the full spec at `dataland-frontend/HOMEPAGE_REWORK_SPEC.md`. Then read every file listed below and verify it matches the spec.
>
> **New files to review:**
>
> - All files in `src/components/resources/landingPage/` (10 section components + `landingContent.ts`)
> - All files in `src/components/resources/productPage/` (8 section components + `productContent.ts`)
> - `src/components/resources/aboutPage/TheAboutCompany.vue`, `TheAboutUpdates.vue`, `TheAboutContact.vue`
> - `src/components/pages/ProductPage.vue`, `NewsletterPage.vue`, `ContactPage.vue`, `TestimonialsPage.vue`
> - `src/components/generics/ProblemSolutionBlock.vue`, `AccessibleCarousel.vue`, `NewsCard.vue`
> - `src/assets/scss/breakpoints.scss`, `src/composables/useBreakpoint.ts`
>
> **Modified files to review:**
>
> - `src/components/pages/LandingPage.vue`, `AboutPage.vue`
> - `src/components/generics/LandingPageHeader.vue`, `LandingPageFooter.vue`
> - `src/components/resources/landingPage/TheIntro.vue`
> - `src/components/resources/aboutPage/TheAboutTeam.vue`, `TheAboutPartners.vue`, `aboutContent.ts`
> - `src/router/index.ts`, `vite.config.ts`
>
> **Check for:**
>
> - Spec compliance (section order, content, layout, breakpoints)
> - No references to deleted files (TheQuotes, TheHowItWorks, TheJoinCampaign, TheStruggle, TheAboutSponsors, TheAboutHero, TheAboutTrustPillars, TheAboutPrinciples, TheAboutEcosystem, TheAboutBottomCTA)
> - `useBreakpoint` used instead of manual resize listeners
> - BEM naming, scoped SCSS, `$bp-*` variables (no magic numbers)
> - ARIA attributes on sections (`role="region"`, `aria-labelledby`)
> - Semantic HTML (`<section>`, `<button>` not `<div>` for clickable elements)
> - No `any` types, no Options API, no PrimeFlex, no `:deep()`, no FormKit
> - Carousel accessibility (pause control, keyboard nav, `aria-live`, `prefers-reduced-motion`)
> - Dropdown menu accessibility (keyboard nav, `aria-expanded`, `role="menu"`)
>
> Output a single numbered issue list with file path, line number, what's wrong, and what the spec requires. If everything passes, say "No issues found."

### 3b — `copywriter` — Copy Accuracy Audit

**Prompt:**

> Read the full spec at `dataland-frontend/HOMEPAGE_REWORK_SPEC.md`. Then read every file containing user-facing text:
>
> - `dataland-frontend/src/components/resources/landingPage/landingContent.ts`
> - `dataland-frontend/src/components/resources/productPage/productContent.ts`
> - `dataland-frontend/src/components/resources/aboutPage/aboutContent.ts`
> - All Vue templates in `src/components/resources/landingPage/`, `src/components/resources/productPage/`, `src/components/resources/aboutPage/` that contain hardcoded text
> - `src/components/pages/ProductPage.vue`, `NewsletterPage.vue`, `ContactPage.vue`, `TestimonialsPage.vue`
> - `src/components/generics/LandingPageHeader.vue`, `LandingPageFooter.vue`
>
> **Check for:**
>
> - Every headline, subheadline, CTA label, quote, attribution, card text matches the spec character-for-character
> - No "campaign" wording remains anywhere
> - CTA labels match: "Try it free", "Get in touch", "Watch member testimonials", "Discover platform features", "Explore use cases", etc.
> - Old labels are gone: "SIGN UP", "I AM INTERESTED", "START YOUR DATALAND JOURNEY", "GET IN TOUCH" (all caps)
> - Typos fixed: "sovereignty" not "soveignty", "easily" not "easliy"
> - Copy review changes from Decision 13 are applied (hero subtext, Why Us headline, etc.)
> - All 12 testimonial quotes match spec exactly
> - All 7 use case titles and descriptions match spec
> - All 3 customer stories (MEAG, NORD/LB, ÖVB) match spec in full
> - Pricing card content matches spec exactly
> - Footer link labels and structure match spec
> - Tone: professional-institutional, no startup jargon, no emojis, no exclamation marks
>
> Output a single numbered issue list. If everything passes, say "No issues found."

---

## Phase 4: Fix Review Issues + CI Verification

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Both Phase 3 reviews complete.

### Tasks

1. **Fix all issues** from Phase 3a (code review) and Phase 3b (copy audit).

2. **Run all locally-runnable CI checks and fix failures iteratively:**
   ```bash
   cd dataland-frontend
   npm run typecheck
   npm run lintci
   npm run formatci
   npm run checkdependencies
   npm run checkcypresscompilation
   npm run testcomponent
   ```
   These checks mirror what `gradle-based-tests` and `frontend-component-tests` run in CI. Fix every failure locally before pushing — do not rely on remote CI to discover issues that can be caught here.

3. **Commit and push** to `feature/rework-about-page`.

4. **Monitor remote CI** for checks that cannot run locally (E2E tests require Docker stack):
   ```bash
   gh run list --branch feature/rework-about-page --limit 1 --json status,conclusion,name
   ```
   If any job fails:
   - Run `gh run view <run-id> --log-failed`
   - Fix locally, push, repeat until green

### Exit Criteria

Remote CI must show **all green** on:
- `gradle-based-tests` (lint, format, typecheck, deps — should already pass from local checks)
- `frontend-component-tests` (should already pass from local `testcomponent`)
- `e2e-tests` (all 4 groups — these are the only checks that require the Docker stack and cannot be fully validated locally)

---

## Phase 5: Screenshot-Based Visual QA

**Dependencies:** Phase 4 complete. Remote CI is green.

### Step 5.1 — Human provides screenshots

1. Start the dev stack or dev server
2. Take screenshots at these viewports:

| Viewport | Pages to Capture |
|----------|-----------------|
| Desktop (1440px+) | Landing page (full scroll), Product page (full scroll), About page (full scroll) |
| Tablet (768-1024px) | Landing page, Product page, About page |
| Mobile (< 768px) | Landing page (hamburger closed + open), Product page, About page |
| Small mobile (< 640px) | Landing hero (CTA stacking), Product pricing section |

### Step 5.2 — `ux-designer` — Screenshot Visual QA

**Prompt:**

> I am providing screenshots of the reworked Dataland pages at multiple viewports. The spec is at `dataland-frontend/HOMEPAGE_REWORK_SPEC.md`. Compare each screenshot against the spec and identify layout, spacing, typography, color, responsive, and accessibility issues. For each issue, provide: description, which screenshot, and the specific file + CSS property to fix.

### Step 5.3 — Fix visual issues

1 `frontend-developer` (Agent X) fixes all issues from the visual review, then:

1. Re-run local CI checks (`typecheck`, `lintci`, `formatci`, `checkdependencies`, `testcomponent`) — fix any regressions.
2. Push and monitor remote CI. If E2E tests fail, inspect logs with `gh run view <run-id> --log-failed`, fix locally, and push again until green.

---

## Phase Summary

```
Phase 1  [Infrastructure + Content]    ████           1 agent, serial
Phase 2  [Build All Pages + Nav/Footer ████████████   1 agent, serial
          + Cleanup + Tests]
Phase 3  [Independent Reviews]         ██████         2 agents, parallel (read-only)
Phase 4  [Fixes + CI Verification]     ██████         1 agent, serial → CI must be GREEN
Phase 5  [Screenshot QA]               ████████       Human + ux-designer + fixes
```

**Total agents:** 5 (+ ux-designer for screenshots)
**Parallel worktrees:** 0
**Branches:** 1 (`feature/rework-about-page`)
**CI verification:** Phase 4 only

---

## Key Principles

1. **One branch, no worktrees.** All agents work on `feature/rework-about-page`. No isolation branches.
2. **Sequential builds.** Phases 1 and 2 run serially to avoid interference on the shared branch.
3. **Parallel reviews are safe.** Phase 3 agents are read-only — they produce issue lists but never edit files.
4. **CI at the end.** Phase 4 is the single CI checkpoint. Implementation isn't done until remote CI is green.
5. **Builder never reviews its own work.** Agent X builds. Agent Y reviews code. The copywriter reviews text.
6. **Spec is the single source of truth.** Every agent references `HOMEPAGE_REWORK_SPEC.md`.

---

## Image Assets Note

Many image assets referenced in the spec (`intro_art.svg`, `arrow_big.svg`, news images, sector icons, customer logos, etc.) may not exist yet. During implementation:

- Use placeholder `[PLACEHOLDER]` comments in templates where images are referenced but not yet available
- For logos that already exist in the codebase (e.g., existing brand logos in TheBrands), reuse those paths
- Create a checklist of missing assets that need to be provided by the design team
- Missing images should NOT block CI — use fallback `alt` text and ensure the layout works with broken image references
