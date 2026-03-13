# Landing Page & About Page Rework — Implementation Plan

**Date:** 2026-03-08
**Spec:** `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`
**Branch:** `feature/rework-about-page`

---

## Agent Roles

| Role | Agent Type | Identity | Rule |
|------|-----------|----------|------|
| **Builder** | `frontend-developer` | Agent X | Implements all code. Never reviews its own output. |
| **Code Reviewer** | `frontend-developer` | Agent Y | Fresh agent, independent from Agent X. Reads spec + finished code. Never saw the build. |
| **Visual Reviewer** | `ux-designer` | — | Two-pass review: (1) source code audit, (2) screenshot-based visual QA from user-provided screenshots. |
| **Copy Reviewer** | `copywriter` | — | Verifies every rendered string against the spec. |

**Critical rule:** Agent Y (code reviewer) must be a **separate invocation** from Agent X (builder). It starts fresh with no build context — genuinely independent eyes.

---

## Phase 1: Infrastructure

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** None — this is the foundation everything else imports.

### Tasks

1. **Create `src/assets/scss/breakpoints.scss`**
   ```scss
   $bp-sm: 640px;
   $bp-md: 768px;
   $bp-lg: 1024px;
   $bp-xl: 1440px;
   ```

2. **Update `vite.config.ts`** — add SCSS `additionalData` so breakpoint variables are available globally:
   ```ts
   css: {
     preprocessorOptions: {
       scss: {
         additionalData: `@use "@/assets/scss/breakpoints" as *;\n`,
       },
     },
   },
   ```

3. **Create `src/composables/useBreakpoint.ts`** — shared reactive breakpoint composable (spec section 2.2).

4. **Update `src/assets/content.json`** — all changes from spec section 5.1:
   - Rename "Join a campaign" → "Frameworks", update headline text array
   - Update "How it works" card 3 text (remove "campaign")
   - Replace "Quotes" section with text-only "Social Proof" quotes
   - Fix typos in "Our principles" ("soveignty" → "sovereignty", "easliy" → "easily")
   - Remove "Claim" section entirely

5. **Update `src/components/resources/aboutPage/aboutContent.ts`** — full replacement per spec section 5.2:
   - Update trust pillar 4: title → "Transparent Technology", new description
   - Add `Principle` interface + `PRINCIPLES` array
   - Add `url?: string` to `AdvisoryPerson` interface
   - Update `ADVISORY_BOARD` with URLs
   - Update `BOTTOM_CTA_COPY` with dual CTA labels
   - Add `SPONSORS` and `PARTNERS` logo arrays

6. **Create `src/components/resources/landingPage/socialProofContent.ts`** — quote data + success story summary data (spec section 3.8).

7. **Create `src/components/resources/successStories/successStoryContent.ts`** — full success story data with `SuccessStory` interface (spec section 3.11).

### Completion check
- All content files compile without TypeScript errors
- No downstream components created yet — just data + infrastructure

---

## Phase 2: Build New Components

**Agent:** 7 `frontend-developer` agents (all Agent X), **parallel worktrees** (`isolation: "worktree"`)
**Dependencies:** Phase 1 must be complete (content files + breakpoints exist).

Launch all 7 in a single message for maximum parallelism.

### Worktree A: `TheTrustBar.vue`

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 3.5 ("TheTrustBar"). Create the file `dataland-frontend/src/components/resources/landingPage/TheTrustBar.vue`. The component is self-contained — logo data is hardcoded in the component (not from content.json). Follow the template, styles, and data exactly as specified. Use BEM naming, scoped SCSS, semantic HTML with `aria-label="Trusted by"`. Use the SCSS breakpoint variables from `src/assets/scss/breakpoints.scss` (`$bp-md`). After creating the file, run `cd dataland-frontend && npx vue-tsc --noEmit` to verify TypeScript.

### Worktree B: `TheDataAccess.vue`

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 3.6 ("TheDataAccess"). Also read the existing `dataland-frontend/src/components/resources/landingPage/TheHowItWorks.vue` to understand the current SlideShow pattern and how content.json data flows in via the `sections` prop. Create `dataland-frontend/src/components/resources/landingPage/TheDataAccess.vue` as a replacement. Key additions: video integration with IntersectionObserver lazy-loading, autoplay muted loop, poster SVG fallback. The video area must gracefully handle a missing mp4 (show only the poster). Use `useBreakpoint` composable instead of manual resize listeners. Follow spec exactly for template structure, styles, and behavior. Run `cd dataland-frontend && npx vue-tsc --noEmit` to verify.

### Worktree C: `TheFrameworks.vue`

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 3.7 ("TheFrameworks"). Also read the existing `dataland-frontend/src/components/resources/landingPage/TheJoinCampaign.vue` to understand the current pattern. Create `dataland-frontend/src/components/resources/landingPage/TheFrameworks.vue` as a replacement. Key changes from TheJoinCampaign: no "campaign" wording, headline updated, CTA changed from `openEmailClient` to Keycloak register, headline reduced from 100px to 64px at desktop. Follow spec exactly. Run `cd dataland-frontend && npx vue-tsc --noEmit` to verify.

### Worktree D: `TheSocialProof.vue`

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 3.8 ("TheSocialProof"). Also read `dataland-frontend/src/components/resources/landingPage/socialProofContent.ts` for the data. Create `dataland-frontend/src/components/resources/landingPage/TheSocialProof.vue`. This replaces TheQuotes — NO YouTube embeds, NO cookie consent. Three sub-sections: (A) quote cards in 2x2 grid (mobile: SlideShow), (B) success story summary cards linking to `/success-stories/:slug`, (C) member process sketch image. Follow card designs exactly (border-radius, shadow, padding, typography). Use existing `SlideShow.vue` component for mobile quotes. Run `cd dataland-frontend && npx vue-tsc --noEmit` to verify.

### Worktree E: `TheAboutPrinciples.vue`

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 4.6 ("TheAboutPrinciples"). Read `dataland-frontend/src/components/resources/aboutPage/aboutContent.ts` for the `PRINCIPLES` data. Create `dataland-frontend/src/components/resources/aboutPage/TheAboutPrinciples.vue`. 3x2 grid (desktop), 2x3 (tablet), 1x6 (mobile). Each card has a 3px solid orange left border (`#ff6813`), icon, bold title, single sentence description. Follow template and styles exactly from spec. Run `cd dataland-frontend && npx vue-tsc --noEmit` to verify.

### Worktree F: `TheAboutEcosystem.vue`

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 4.8 ("TheAboutEcosystem"). Read `dataland-frontend/src/components/resources/aboutPage/aboutContent.ts` for the `SPONSORS` and `PARTNERS` data. Also read the existing `TheAboutSponsors.vue` and `TheAboutPartners.vue` to understand the current `LogoChip` component usage. Create `dataland-frontend/src/components/resources/aboutPage/TheAboutEcosystem.vue` that merges both into one section with two labeled subsections. Follow template and styles exactly. Run `cd dataland-frontend && npx vue-tsc --noEmit` to verify.

### Worktree G: `SuccessStoryPage.vue` + Route

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`, section 3.11 ("Customer Success Stories Page"). Read `dataland-frontend/src/components/resources/successStories/successStoryContent.ts` for the data. Create `dataland-frontend/src/components/pages/SuccessStoryPage.vue` with all sections: hero, challenge, process (with process sketch image), result, pull quote, CTA ("Start Using Dataland" for registration + "Read More Stories" linking back to landing page). The page uses the same `LandingPageHeader` and footer as landing/about pages. Use `meta: { layout: 'landing' }` on the route. Also update `dataland-frontend/src/router/index.ts` to add the `/success-stories/:slug` route. Handle unknown slugs gracefully (redirect to landing page). Run `cd dataland-frontend && npx vue-tsc --noEmit` to verify.

### After all worktrees complete
Merge each worktree branch into `feature/rework-about-page` sequentially. Resolve any conflicts (unlikely since all create new files).

---

## Phase 3: Modify Existing Components & Wire Up

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Phases 1 and 2 complete, all new components merged.

### Tasks (execute in this order)

1. **Modify `TheIntro.vue`** (spec section 3.3):
   - Add visible `<label>` above search bar
   - Fix Back button: `<div>` → `<button type="button">`
   - Add two CTA buttons below search (Create Free Account + Get in Touch)
   - Replace manual resize listener with `useBreakpoint`
   - Remove direct DOM manipulation of `.header` in focus/blur handlers

2. **Modify `TheStruggle.vue`** (spec section 3.4):
   - Reduce headline font-size from 100px to 64px at desktop
   - Scale: 64px (xl) → 48px (lg) → 40px (md) → 32px (sm)

3. **Modify `TheGetInTouch.vue`** (spec section 3.10):
   - CTA label: `"GET IN TOUCH"` → `"Get in Touch"` (title case)

4. **Modify `TheBrands.vue`** (spec section 3.9):
   - Replace magic number breakpoints with SCSS `$bp-*` variables

5. **Modify `PersonCard.vue`** (spec section 4.7):
   - Render organisation as `<a>` link when `url` prop is provided

6. **Modify `TheAboutBottomCTA.vue`** (spec section 4.9):
   - Add second CTA button
   - Primary: "Talk to Our Team" → opens ContactInquiryModal
   - Secondary: "Learn More About Our Data" → navigates to `{ path: '/', hash: '#frameworks' }`

7. **Modify `LandingPageHeader.vue`** (spec section 2.3 + 2.4):
   - Add skip-to-content link (first child of `<header>`)
   - Add hamburger menu for mobile (< 768px)
   - Slide-down overlay with nav links + auth section
   - Accessibility: `aria-expanded`, `aria-controls`, `aria-label`

8. **Update `LandingPage.vue`** (spec section 3.2):
   - Add `id="main-content"` to `<main>`
   - New section order: TheIntro → TheStruggle → TheTrustBar → TheDataAccess → TheFrameworks → TheSocialProof → TheBrands → TheGetInTouch → ContactInquiryModal
   - Update imports: remove TheQuotes/TheHowItWorks/TheJoinCampaign, add new components

9. **Update `AboutPage.vue`** (spec section 4.2):
   - Add `id="main-content"` to `<main>`
   - New section order: ContactInquiryModal → TheAboutHero → TheAboutTrustPillars → TheAboutTeam → TheAboutPrinciples → TheAboutAdvisoryBoard → TheAboutEcosystem → TheAboutBottomCTA
   - Update imports: remove TheAboutSponsors/TheAboutPartners, add TheAboutPrinciples/TheAboutEcosystem

10. **Delete old components:**
    - `src/components/resources/landingPage/TheQuotes.vue`
    - `src/components/resources/landingPage/TheHowItWorks.vue`
    - `src/components/resources/landingPage/TheJoinCampaign.vue`
    - `src/components/resources/aboutPage/TheAboutSponsors.vue`
    - `src/components/resources/aboutPage/TheAboutPartners.vue`

11. **Run validation:**
    ```bash
    cd dataland-frontend
    npm run typecheck
    npm run lint
    ```
    Fix any errors before proceeding.

---

## Phase 4: Independent Reviews (3 agents in parallel)

**Dependencies:** Phase 3 complete, code compiles and lints clean.

Launch all 3 review agents in a **single message** for parallel execution. **None of these are Agent X.**

### 4a — `frontend-developer` (Agent Y) — Independent Code Review

**Prompt:**
> You are reviewing code that someone else wrote. You have never seen this code before. Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md` in full. Then read every file listed below. For each file, verify it matches the spec exactly. Produce a numbered issue list with file path, line number, what's wrong, and what the spec requires.
>
> **Files to review (created):**
> - `src/assets/scss/breakpoints.scss`
> - `src/composables/useBreakpoint.ts`
> - `src/components/resources/landingPage/TheTrustBar.vue`
> - `src/components/resources/landingPage/TheDataAccess.vue`
> - `src/components/resources/landingPage/TheFrameworks.vue`
> - `src/components/resources/landingPage/TheSocialProof.vue`
> - `src/components/resources/landingPage/socialProofContent.ts`
> - `src/components/resources/aboutPage/TheAboutPrinciples.vue`
> - `src/components/resources/aboutPage/TheAboutEcosystem.vue`
> - `src/components/pages/SuccessStoryPage.vue`
> - `src/components/resources/successStories/successStoryContent.ts`
>
> **Files to review (modified):**
> - `src/components/pages/LandingPage.vue`
> - `src/components/pages/AboutPage.vue`
> - `src/components/generics/LandingPageHeader.vue`
> - `src/components/resources/landingPage/TheIntro.vue`
> - `src/components/resources/landingPage/TheStruggle.vue`
> - `src/components/resources/landingPage/TheGetInTouch.vue`
> - `src/components/resources/landingPage/TheBrands.vue`
> - `src/components/resources/aboutPage/aboutContent.ts`
> - `src/components/resources/aboutPage/TheAboutBottomCTA.vue`
> - `src/components/resources/aboutPage/PersonCard.vue`
> - `src/assets/content.json`
> - `src/router/index.ts`
> - `vite.config.ts`
>
> **Check for:**
> - Spec compliance (section order, content, layout, breakpoints, font sizes, spacing)
> - Correct imports — no references to deleted files (TheQuotes, TheHowItWorks, TheJoinCampaign, TheAboutSponsors, TheAboutPartners)
> - `useBreakpoint` used instead of manual resize listeners
> - BEM naming convention followed
> - SCSS uses `$bp-*` variables, not magic numbers
> - ARIA attributes present on all sections (`role="region"`, `aria-labelledby`)
> - Semantic HTML (`<section>`, `<button>` not `<div>` for clickable elements)
> - No `any` types, no Options API, no PrimeFlex, no `:deep()`, no FormKit
> - `[PLACEHOLDER]` comments present on all placeholder content
>
> Also run: `cd dataland-frontend && npm run typecheck && npm run lint`
>
> Output a single numbered issue list. If everything passes, say "No issues found."

### 4b — `copywriter` — Copy Accuracy Audit

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md` in full. Then read every file that contains rendered text:
>
> - `dataland-frontend/src/assets/content.json`
> - `dataland-frontend/src/components/resources/aboutPage/aboutContent.ts`
> - `dataland-frontend/src/components/resources/landingPage/socialProofContent.ts`
> - `dataland-frontend/src/components/resources/successStories/successStoryContent.ts`
> - All Vue templates in `src/components/resources/landingPage/` and `src/components/resources/aboutPage/` that contain hardcoded text (headlines, labels, button text)
> - `src/components/pages/SuccessStoryPage.vue`
>
> **Check for:**
> - Every headline, subheadline, CTA label, quote, attribution, and card description matches the spec character-for-character
> - No "campaign" wording remains anywhere
> - CTA labels use correct casing: "Get in Touch" (title case), "Create Free Account", "Talk to Our Team", "Learn More About Our Data"
> - Old labels are gone: "SIGN UP", "I AM INTERESTED", "START YOUR DATALAND JOURNEY", "GET IN TOUCH" (all caps)
> - Typos fixed: "sovereignty" not "soveignty", "easily" not "easliy"
> - Trust pillar 4 title is "Transparent Technology" (not "AI at non-profit scale")
> - "Quotes" section renamed to "Social Proof" in content.json
> - "Join a campaign" renamed to "Frameworks" in content.json
> - "Claim" section removed from content.json
> - Tone is professional-institutional, no startup jargon, no emojis, no exclamation marks
>
> Output a single numbered issue list. If everything passes, say "No issues found."

### 4c — `ux-designer` — Source Code UX Audit (Pass 1)

**Prompt:**
> Read the spec at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md` in full. Then read every new and modified Vue SFC and SCSS file (see the file lists in spec section 6.1 and 6.2). All files are under `dataland-frontend/src/`.
>
> **Check for:**
> - Responsive behavior at all 4 breakpoints ($bp-sm: 640px, $bp-md: 768px, $bp-lg: 1024px, $bp-xl: 1440px)
> - Font sizes match spec at each breakpoint (e.g., hero headline: 100px xl, 64px lg, 48px md, 40px mobile)
> - Section headline font sizes reduced from 100px to 64px at desktop (TheStruggle, TheFrameworks)
> - Card designs match spec exactly (border-radius, box-shadow, padding, typography per card type)
> - TrustBar: grayscale filter + hover-to-color transition on logos
> - TheSocialProof: quote cards have decorative opening quotation mark (48px, orange, 0.3 opacity)
> - TheAboutPrinciples: 3px solid orange left border on cards
> - Hamburger menu: appears below 768px, hides nav links, slide-down overlay, close on link click
> - Skip-to-content link: visually hidden, visible on focus, positioned correctly
> - Video: IntersectionObserver lazy-load, autoplay muted loop, poster fallback for missing mp4
> - Mobile: CTA buttons stack vertically below 640px
> - Section backgrounds match spec (orange for DataAccess, dark for GetInTouch, grey for Struggle, white for others)
> - Spacing: section padding matches spec values
> - Accessibility: `role="region"`, `aria-labelledby`, `aria-label` on interactive elements, `<button>` for clickable elements
>
> Output a single numbered issue list with file path, what's wrong, and what the spec requires. If everything passes, say "No issues found."

---

## Phase 5: Fix Review Issues

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** All Phase 4 reviews complete.

**Prompt:**
> The following issues were found during independent code review, copy audit, and UX audit of the landing/about page rework. Fix each one. The spec is at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`.
>
> [Paste combined issue lists from Phase 4a + 4b + 4c here]
>
> After fixing all issues, run `cd dataland-frontend && npm run typecheck && npm run lint` to verify.

---

## Phase 6: Screenshot-Based Visual QA

**Dependencies:** Phase 5 complete. Code compiles and lints clean.

### Step 6.1 — You (human) provide screenshots

1. Start the dev server: `cd dataland-frontend && npm run dev`
2. Open `https://local-dev.dataland.com/` in a browser
3. Take screenshots at these viewports:

| Viewport | Pages to Capture |
|----------|-----------------|
| Desktop (1440px+) | Landing page (full scroll), About page (full scroll), all 3 Success Story pages |
| Tablet (768-1024px) | Landing page, About page |
| Mobile (< 768px) | Landing page (hamburger closed), Landing page (hamburger open), About page |
| Small mobile (< 640px) | Landing page hero (check CTA stacking), About page |

4. Save screenshots to a local folder or provide them directly in the conversation.

### Step 6.2 — `ux-designer` — Screenshot Visual QA (Pass 2)

**Prompt:**
> I am providing screenshots of the reworked Dataland landing page, about page, and success story pages at multiple viewports. The spec is at `dataland-frontend/LANDING_ABOUT_REWORK_SPEC.md`. The visual flow diagrams in the spec appendix show exactly what each page should look like.
>
> For each screenshot, compare against the spec and identify:
> - **Layout issues:** Elements not aligned, wrong grid columns, unexpected wrapping
> - **Spacing issues:** Sections too close/far apart, padding inconsistent with spec
> - **Typography issues:** Headlines too large/small, wrong font weight, line height off
> - **Color issues:** Wrong background color, wrong text color, missing grayscale filter
> - **Missing elements:** Components not rendering, icons not showing, images broken
> - **Responsive issues:** Content overflowing, hamburger not showing/hiding correctly, elements not stacking as expected
> - **Interaction hints:** Buttons that look unclickable, hover states missing, CTA hierarchy unclear
> - **Visual polish:** Inconsistent border-radius, shadows not matching spec, rough edges
>
> For each issue, provide: description, which screenshot, and the specific file + CSS property to fix.
>
> [Attach screenshots here]

---

## Phase 7: Final Fixes

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Phase 6 complete.

Fix all issues from the screenshot review. If changes are significant (layout restructuring, major responsive fixes), take fresh screenshots and do a quick re-check with the `ux-designer`.

---

## Phase Summary

```
Phase 1  [Infrastructure]           ████           1 frontend-developer (Agent X), serial
Phase 2  [New Components]           ████████████   7 frontend-developer (Agent X), parallel worktrees
Phase 3  [Modify + Wire Up]         ██████████     1 frontend-developer (Agent X), serial
Phase 4  [Independent Reviews]      ████████       3 agents in parallel (Agent Y + copywriter + ux-designer)
Phase 5  [Fix Review Issues]        ██████         1 frontend-developer (Agent X), serial
Phase 6  [Screenshot QA]            ████████       Human screenshots → ux-designer reviews images
Phase 7  [Final Fixes]              ████           1 frontend-developer (Agent X), serial
```

## Key Principles

1. **Builder never reviews its own work.** Agent X builds. Agent Y reviews code. The ux-designer reviews visuals. The copywriter reviews copy.
2. **Two-pass UX review.** Pass 1 (Phase 4c) catches structural issues from source code. Pass 2 (Phase 6) catches rendering issues from screenshots.
3. **Parallel where possible.** Phase 2 (7 worktrees) and Phase 4 (3 reviewers) run concurrently.
4. **Human-in-the-loop for screenshots.** The user provides screenshots at defined viewports. The ux-designer agent analyzes them against the spec.
5. **Spec is the single source of truth.** Every agent references `LANDING_ABOUT_REWORK_SPEC.md`. No verbal instructions.
