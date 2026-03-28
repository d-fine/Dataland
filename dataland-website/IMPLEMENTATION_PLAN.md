# Dataland Website — Implementation Plan

**Spec:** `dataland-website/SPEC.md`
**Branch:** `feature/rework-about-page`

---

## Lessons Learned from Previous Implementation

The Vue rework plan evolved through two iterations:

1. **First attempt:** 7 phases, 7 parallel worktrees, 3 review agents → CI failures from isolated builds, branch proliferation, merge conflicts
2. **Second attempt:** 4 phases, no worktrees, 1 branch → worked but was still within the Vue SPA, adding complexity rather than reducing it

This plan migrates out of the SPA entirely. The Astro migration is simpler by nature (static HTML, Tailwind utilities, no PrimeVue), so the phase structure is leaner — but the agent discipline and review rigor carry over.

---

## Architecture

```
dataland-website/
├── SPEC.md                      # Migration specification
├── astro.config.mjs
├── package.json
├── tsconfig.json
├── public/
│   ├── static/                  # Copied from dataland-frontend/public/static/
│   │   ├── logos/
│   │   ├── icons/
│   │   ├── images/
│   │   └── about/
│   ├── fonts/                   # IBM Plex Sans
│   ├── favicon.svg
│   ├── apple-touch-icon.png
│   └── site.webmanifest
└── src/
    ├── layouts/
    │   └── Base.astro           # HTML shell: <head>, Cookiebot, nav, footer
    ├── components/
    │   ├── Header.astro         # Logo + Product + About + Login
    │   ├── MobileNav.vue        # Vue island: hamburger + drawer
    │   ├── Footer.astro         # 4-column footer
    │   ├── CompanySearch.vue    # Vue island: LEI/company search bar
    │   └── YouTubeEmbed.astro   # Cookiebot-gated YouTube iframe
    ├── pages/
    │   ├── index.astro          # Landing page (9 sections inline)
    │   ├── about.astro          # About page (5 sections inline)
    │   ├── product.astro        # Product page (8 sections inline)
    │   ├── testimonials.astro   # Video testimonial grid
    │   └── success-stories/
    │       └── [...slug].astro  # Dynamic route from MDX content
    ├── content/
    │   └── success-stories/     # MDX files (already created)
    │       ├── meag-sfdr-data-gaps.mdx
    │       ├── nordlb-primary-esg-data.mdx
    │       └── oeffentliche-pai-validation.mdx
    ├── data/
    │   ├── testimonials.ts      # 12 testimonials with YouTube IDs
    │   ├── frameworks.ts        # 6 framework cards
    │   ├── members.ts           # 14 trusted-by logos
    │   ├── news.ts              # 9 news items with LinkedIn URLs
    │   ├── whyUs.ts             # 4 problem/solution pairs
    │   ├── product.ts           # Features, use cases, pricing, customer stories, doc links, URLs
    │   └── about.ts             # Team (3), partners (4), company copy, contact info
    └── styles/
        └── global.css           # Tailwind directives + brand CSS custom properties
```

## Key Design Decisions

### 1. Inline sections, not component-per-section
Each page file contains its sections directly as HTML+Tailwind. Extract a component only when it's reused across pages (Header, Footer, YouTubeEmbed) or needs client-side JS (MobileNav, CompanySearch).

### 2. Data files copied from Vue codebase
Content arrays are copied verbatim from `dataland-frontend/src/components/resources/` into `src/data/*.ts`. Remove Vue-specific imports, keep types and data. Astro imports at build time.

### 3. Only two Vue islands
- **MobileNav.vue** — hamburger + drawer (`client:media="(max-width: 1023px)"`)
- **CompanySearch.vue** — LEI search with API call (`client:visible`)

Everything else is static HTML. Total shipped JS: ~5–10 kB.

### 4. CSS scroll-snap replaces JS carousels
Testimonials use `overflow-x: auto` + `scroll-snap-type: x mandatory`. Trusted-by and news sections use static grids. No carousel JS, no pause buttons, no ARIA live regions.

### 5. No nav dropdowns
Header has plain links for Product and About (2 items don't justify dropdown complexity).

### 6. Legal pages stay in SPA
Footer links to `/terms`, `/imprint`, `/dataprivacy` point to the existing Vue app.

## LoC Estimate

| Area | Current (Vue) | Target (Astro) | Reduction |
|------|--------------|----------------|-----------|
| Page components (5 pages) | ~840 | ~600 | ~30% |
| Section components (~30) | ~2,870 | 0 (inlined) | 100% |
| Layout + nav + footer | ~725 | ~250 | ~65% |
| Content/data files | ~790 | ~600 | ~25% |
| Styling (SCSS) | ~3,500 | ~0 (Tailwind utilities) | ~100% |
| Vue islands | 0 | ~150 | new |
| **Total** | **~8,725** | **~1,600** | **~80%** |

---

## CI Verification Reference

### Astro CI Gate (dataland-website)

```bash
cd dataland-website
npm run build              # Astro static build — catches all template/import errors
npx astro check            # TypeScript checking for .astro files
```

The Astro build is the primary CI gate. Unlike the Vue SPA (which has separate typecheck, lint, format, and component test steps), Astro's build catches most issues in one pass: broken imports, type errors in frontmatter, invalid HTML, missing data files.

### Vue SPA CI Gate (dataland-frontend — for cleanup verification)

After removing migrated files from the SPA, verify it still builds:

```bash
cd dataland-frontend
npm run typecheck          # vue-tsc --noEmit
npm run lintci             # ESLint — fails on ANY warning
npm run formatci           # Prettier formatting check
npm run checkdependencies  # Unused/missing deps
npm run testcomponent      # Cypress component tests
```

### What Breaks CI Most Often

1. **Dangling imports** — SPA files still importing deleted components
2. **Router references** — routes pointing to deleted page components
3. **Component test failures** — tests referencing removed components or changed selectors
4. **Missing assets** — images referenced in templates but not copied to `public/static/`

---

## Agent Roles

| Role | Agent Type | Identity | Rule |
|---|---|---|---|
| **Builder** | `frontend-developer` | Agent X | Implements all code in both codebases. |
| **Code Reviewer** | `frontend-developer` | Agent Y | Fresh agent, independent from Agent X. Reads spec + finished code. Never saw the build. |
| **Copy Reviewer** | `copywriter` | — | Verifies every rendered string against the spec and Vue source content files. |

**Critical rules:**

- All agents work on the **same branch** (`feature/rework-about-page`). No worktrees, no feature branches.
- Agent Y (code reviewer) must be a **separate invocation** from Agent X (builder).
- Review agents (Phase 3) are **read-only** — they read code and produce issue lists but do not edit files.
- **Spec is the single source of truth** for structure and layout. **Vue content files are the source of truth** for copy text.

---

## Phase 1: Build Astro Site (Sessions 1–6)

**Agent:** 1 `frontend-developer` (Agent X), serial

All sessions run sequentially on one branch. Each session ends with `npm run build` succeeding.

### Session 1: Project Scaffold + Assets

**Deliverable:** Astro project builds and serves pages with shared layout, nav, and footer.

1. Initialize Astro project (`npm create astro@latest`), add integrations:
   - `@astrojs/tailwind`
   - `@astrojs/vue`
   - `@astrojs/sitemap`
   - Output: `static`
2. Create `src/styles/global.css` with Tailwind directives and brand CSS custom properties (see SPEC: Brand & Tokens)
3. Create `src/layouts/Base.astro`:
   - `<head>`: charset, viewport, IBM Plex Sans font, Cookiebot script, `<title>` and `<meta>` slots
   - Skip-to-content link
   - `<Header />` + `<slot />` + `<Footer />`
4. Create `src/components/Header.astro`:
   - Logo (links to `/`), "Product" link, "About" link, "Login" link, "Try it free" button
   - Sticky, 72px height, surface-50 background, border-bottom
   - Hamburger button visible below `lg:`, triggers MobileNav
5. Create `src/components/MobileNav.vue`:
   - Props: `links` array
   - Slide-down overlay with all nav links + CTAs
   - Close on link click, close button, or Escape key
   - `client:media="(max-width: 1023px)"`
6. Create `src/components/Footer.astro`:
   - 4-column grid layout matching SPEC footer table
   - Legal links to SPA, LinkedIn external link, copyright with dynamic year
   - Responsive: 4-col → 2-col → 1-col
7. Create empty page shells (`index.astro`, `product.astro`, `about.astro`, `testimonials.astro`, `success-stories/[...slug].astro`) — each wraps `<Base>` with a placeholder heading
8. Copy static assets from `dataland-frontend/public/static/` → `public/static/`:
   - `logos/`, `icons/`, `images/`, `about/`, fonts, favicons
9. **Verify:** `npm run build` succeeds, `npm run dev` serves all routes with header/footer

### Session 2: Data Files + Landing Page

**Deliverable:** Landing page with all 9 sections, fully responsive.

1. Create data files by copying from Vue codebase:
   - `src/data/whyUs.ts` ← `WHY_US_PAIRS` from `landingContent.ts`
   - `src/data/members.ts` ← `TRUSTED_BY_LOGOS` from `landingContent.ts`
   - `src/data/testimonials.ts` ← `TESTIMONIALS` from `landingContent.ts` + YouTube IDs
   - `src/data/frameworks.ts` ← `FRAMEWORK_CARDS` from `landingContent.ts`
   - `src/data/news.ts` ← `NEWS_ITEMS` from `landingContent.ts` + all `URL_NEWS_*` constants
   - Also extract: `CUSTOMER_STORY_SUMMARIES`, `SECTOR_TILES`

2. Build `index.astro` — all 9 sections inline:
   - **Hero:** Two-column grid (`lg:grid-cols-[60%_40%]`), headline with orange `<span>`, subtext, 2 CTAs, platform screenshot with 3D transform. Mobile: stack, reduce rotation.
   - **LEI Search:** Centered container, `<CompanySearch client:visible />` Vue island. Calls `GET /api/companies/names`, autocomplete after 4 chars. Max-width 520px.
   - **Why Us:** Iterate `WHY_US_PAIRS`. Each pair: 3-col grid (`lg:grid-cols-[40%_20%_40%]`), arrow SVG in center (`aria-hidden`). Mobile: stack, hide arrow, add divider. 3 CTA buttons below.
   - **Trusted By:** Iterate `TRUSTED_BY_LOGOS`. Logo grid with `items-center`, all logos `max-h-12` (normalize visual size). Static grid, not carousel.
   - **Customer Stories:** Iterate `CUSTOMER_STORY_SUMMARIES`. 3 cards: logo, orange pill tag, text, link. `lg:grid-cols-3`.
   - **Testimonials:** Iterate `TESTIMONIALS`. CSS scroll-snap container (`overflow-x-auto scroll-snap-x-mandatory`), each card `scroll-snap-align: start`, `min-w-[280px]`. Quote text (italic), author (bold), affiliation (muted). CTA button → `/testimonials`.
   - **Frameworks:** Iterate `FRAMEWORK_CARDS`. `lg:grid-cols-3` grid. Each card: title, subtitle, description, orange corner accent (`clip-path: polygon(100% 0, 0 0, 100% 100%)` pseudo-element). Bottom text + CTA.
   - **Customer Profiles:** Iterate `SECTOR_TILES`. CSS Grid weighted mosaic (12-col base). Row 1: 3 XL tiles (cols 1-4, 5-8, 9-12). Row 2: 3 L tiles. Row 3: 5 S tiles. Tablet: uniform 3-col. Mobile: 2-col.
   - **News & Insights:** Iterate `NEWS_ITEMS`. 3-col grid. Each card: near-square image, title, date, "Read more" link (`target="_blank" rel="noopener noreferrer"`). Mobile: 1-col.

3. **Verify:** `npm run build` succeeds

### Session 3: Product Page

**Deliverable:** Product page with all 8 sections, fully responsive.

1. Create `src/data/product.ts` by copying from `productContent.ts`:
   - `HOW_IT_WORKS_BLOCKS`, `GETTING_DATA_BLOCKS`, `FEATURE_CARDS`, `USE_CASES`
   - `CUSTOMER_STORIES_DETAILED` (MEAG, NORD/LB, ÖVB with full challenge/solution/value/quote)
   - `VALUE_PROPOSITIONS`, `PRICING_CARD`, `CREDITS_VISUAL`, `PRICING_BOTTOM_NOTE`
   - `DOCUMENTATION_LINKS`, all `URL_DOC_*` constants

2. Build `product.astro` — all 8 sections inline:
   - **Intro:** Centered headline, short declarative fragments, `max-w-4xl mx-auto`
   - **How It Works** (`#how-it-works`): 3 problem→solution rows, same pattern as landing Why Us
   - **Getting Data** (`#getting-data`): 2 problem→solution rows
   - **Features** (`#features`): 6 cards in `lg:grid-cols-3`, same card style as framework cards
   - **Use Cases** (`#use-cases`): 7 blocks, each with title + description text
   - **Customer Stories** (`#customer-stories`, `#meag`, `#nordlb`, `#ovbraunschweig`): 3 detailed cards. Each: `lg:grid-cols-[24%_76%]`, logo left, content right. Mobile: single column.
   - **Membership & Pricing** (`#membership-pricing`): 3 value prop icons, then 2-col pricing card + credits visual. Mobile: stack.
   - **Documentation** (`#documentation`): 9 links as pills. 2 CTAs.

3. **Verify:** `npm run build` succeeds

### Session 4: About Page

**Deliverable:** About page with all 5 sections, fully responsive.

1. Create `src/data/about.ts` by copying from `aboutContent.ts`:
   - `LEADERSHIP_TEAM` (update: Andreas Höcherl replaces Andreas Pusch)
   - `PARTNERS` (extend: add Eskua AI and Keynum alongside FACT and ISS)
   - `COMPANY_COPY`
   - Contact constants from `productContent.ts`

2. Build `about.astro` — all 5 sections inline:
   - **Company** (`#company`): 2-col, logos left, text right. Mobile: stack.
   - **Team** (`#team`): Dark blue bg (`#0f3a82`), white text. 3 cards with photos, name, role, email/LinkedIn.
   - **Partners** (`#partners`): 4 partner logos. Keynum: dark card or CSS filter.
   - **Updates** (`#updates`): Static 3×3 grid of news cards.
   - **Contact** (`#contact`): Dark bg, 2-col with contact info + visual-only form.

3. **Verify:** `npm run build` succeeds

### Session 5: Testimonials Page + YouTube Integration

**Deliverable:** Testimonials page with 12 Cookiebot-gated YouTube embeds.

1. Create `src/components/YouTubeEmbed.astro`:
   - Props: `videoId`, `title`
   - Cookiebot gating via CSS classes: `cookieconsent-optin-marketing` / `cookieconsent-optout-marketing`
   - Responsive 16:9 aspect ratio (`aspect-video`)

2. Build `testimonials.astro`:
   - Headline + subheadline
   - `lg:grid-cols-3 md:grid-cols-2` grid of 12 video cards
   - Each card: `<YouTubeEmbed>`, name, affiliation

3. **Verify:** `npm run build` succeeds

### Session 6: Success Stories

**Deliverable:** MDX-driven success story pages.

1. Configure Astro content collection in `src/content/config.ts`
2. Verify existing MDX files have correct frontmatter
3. Build `success-stories/[...slug].astro` template:
   - Title, company type tag, framework tag
   - Challenge / Process / Result sections
   - Quote box with attribution
   - CTA: back to product page
4. Link from product page customer story cards to `/success-stories/{slug}`

5. **Verify:** `npm run build` succeeds, all 3 story routes resolve

---

## Phase 2: Independent Reviews (2 agents in parallel)

**Dependencies:** Phase 1 complete (all 6 sessions done, Astro builds cleanly).
**Both agents are read-only** — they produce issue lists but do not edit files.

Launch both in a **single message**.

### 2a — `frontend-developer` (Agent Y) — Code Review

**Prompt:**

> You are reviewing code that someone else wrote. You have never seen this code before. Read the full spec at `dataland-website/SPEC.md`. Then read every file in `dataland-website/src/` and verify it matches the spec.
>
> **Files to review:**
>
> - `src/layouts/Base.astro`
> - `src/components/Header.astro`, `Footer.astro`, `MobileNav.vue`, `CompanySearch.vue`, `YouTubeEmbed.astro`
> - `src/pages/index.astro`, `product.astro`, `about.astro`, `testimonials.astro`, `success-stories/[...slug].astro`
> - All files in `src/data/`
> - `src/content/config.ts`
> - `src/styles/global.css`
> - `astro.config.mjs`, `package.json`, `tsconfig.json`
>
> **Check for:**
>
> - **Spec compliance:** Section order, content, layout grids, responsive breakpoints all match SPEC.md
> - **Astro best practices:** No unnecessary `client:load`, no `<script>` tags where frontmatter suffices, no component-per-section anti-pattern
> - **Tailwind best practices:** No `@apply`, no arbitrary values where Tailwind scale exists (`rounded-[8px]` → `rounded-lg`), mobile-first responsive prefixes, brand tokens via CSS custom properties
> - **Vue islands:** Only MobileNav and CompanySearch use `client:*` directives. No PrimeVue imports. Minimal JS footprint.
> - **Accessibility:** `aria-label` on sections, `aria-hidden` on decorative elements, skip-to-content link, semantic HTML (`<section>`, `<nav>`, `<main>`, `<header>`, `<footer>`), `motion-reduce:` on animations, touch targets ≥ 44px
> - **SEO:** Each page has unique `<title>` and `<meta name="description">`, heading hierarchy (one `<h1>` per page), OG tags
> - **Links:** Footer legal links point to SPA (`/terms`, `/imprint`, `/dataprivacy`), external links have `target="_blank" rel="noopener noreferrer"`, anchor links on product page resolve
> - **No dead code:** No unused imports, no commented-out blocks, no placeholder TODOs
> - **Data file accuracy:** Content arrays in `src/data/*.ts` match the Vue source files character-for-character (compare against `dataland-frontend/src/components/resources/`)
>
> Output a single numbered issue list with file path, line number (if applicable), what's wrong, and what the spec requires. If everything passes, say "No issues found."

### 2b — `copywriter` — Copy Accuracy Audit

**Prompt:**

> You are auditing every user-facing string in a new Astro website to verify it matches the source content. Read these two sets of files:
>
> **Source of truth (Vue content files):**
> - `dataland-frontend/src/components/resources/landingPage/landingContent.ts`
> - `dataland-frontend/src/components/resources/productPage/productContent.ts`
> - `dataland-frontend/src/components/resources/aboutPage/aboutContent.ts`
> - `dataland-frontend/src/components/resources/successStories/successStoryContent.ts`
>
> **Astro files to audit:**
> - `dataland-website/src/data/*.ts` (all data files)
> - `dataland-website/src/pages/*.astro` (all page templates — check inline headlines, CTAs, labels)
> - `dataland-website/src/components/Header.astro`, `Footer.astro`
> - `dataland-website/src/content/success-stories/*.mdx`
>
> **Check for:**
>
> - Every headline, CTA label, quote, attribution, card text, and description in the Astro files matches the Vue source files exactly
> - No old copy remains: "SIGN UP", "I AM INTERESTED", "START YOUR DATALAND JOURNEY", "GET IN TOUCH" (all-caps variants)
> - No "campaign" wording
> - CTA labels match spec: "Try it free", "Get in touch", "Watch member testimonials"
> - All 12 testimonial quotes match source
> - All 7 use case titles and descriptions match source
> - All 3 detailed customer stories (MEAG, NORD/LB, ÖVB) match source in full
> - Pricing card content matches source
> - Footer link labels match spec
> - Team member names, roles, and bios are up to date (Andreas Höcherl, not Andreas Pusch)
> - Partner list is complete: FACT, ISS, Eskua AI, Keynum
> - News items: all 9 titles, dates, and LinkedIn URLs match source
> - Tone: professional-institutional, no startup jargon, no emojis, no exclamation marks
>
> Output a single numbered issue list. If everything passes, say "No issues found."

---

## Phase 3: Fix Review Issues + Astro CI

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Both Phase 2 reviews complete.

### Tasks

1. **Fix all issues** from Phase 2a (code review) and Phase 2b (copy audit).

2. **Run Astro CI checks:**
   ```bash
   cd dataland-website
   npm run build
   npx astro check
   ```
   Fix any failures iteratively.

3. **Verify all routes render:**
   ```bash
   npm run dev
   # Check: /, /product, /about, /testimonials, /success-stories/meag-sfdr-data-gaps
   ```

---

## Phase 4: Vue SPA Cleanup + SPA CI Verification

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Phase 3 complete (Astro site builds and reviews are resolved).

This phase removes all migrated marketing page code from `dataland-frontend`. The SPA must still build and pass CI after removal.

### 4.1 Remove Migrated Page Components

**Delete page components** (these are now served by Astro):

| File to delete | Replaced by |
|---|---|
| `src/components/pages/LandingPage.vue` | `dataland-website/src/pages/index.astro` |
| `src/components/pages/AboutPage.vue` | `dataland-website/src/pages/about.astro` |
| `src/components/pages/ProductPage.vue` | `dataland-website/src/pages/product.astro` |
| `src/components/pages/TestimonialsPage.vue` | `dataland-website/src/pages/testimonials.astro` |
| `src/components/pages/NewsletterPage.vue` | Removed (was static placeholder) |
| `src/components/pages/ContactPage.vue` | Merged into `/about#contact` |

### 4.2 Remove Migrated Section Components

**Delete all landing page section components:**
- `src/components/resources/landingPage/TheIntro.vue`
- `src/components/resources/landingPage/TheFindLei.vue`
- `src/components/resources/landingPage/TheWhyUs.vue`
- `src/components/resources/landingPage/TheTrustedBy.vue`
- `src/components/resources/landingPage/TheCustomerStories.vue`
- `src/components/resources/landingPage/TheTestimonials.vue`
- `src/components/resources/landingPage/TheFrameworks.vue`
- `src/components/resources/landingPage/TheCustomerProfiles.vue`
- `src/components/resources/landingPage/TheNewsInsights.vue`
- `src/components/resources/landingPage/landingContent.ts`

**Delete old landing page components** (if not already removed by rework):
- `src/components/resources/landingPage/TheQuotes.vue`
- `src/components/resources/landingPage/TheHowItWorks.vue`
- `src/components/resources/landingPage/TheJoinCampaign.vue`
- `src/components/resources/landingPage/TheStruggle.vue`

**Delete all product page section components:**
- `src/components/resources/productPage/ProductIntro.vue`
- `src/components/resources/productPage/ProductHowItWorks.vue`
- `src/components/resources/productPage/ProductGettingData.vue`
- `src/components/resources/productPage/ProductFeatures.vue`
- `src/components/resources/productPage/ProductUseCases.vue`
- `src/components/resources/productPage/ProductCustomerStories.vue`
- `src/components/resources/productPage/ProductMembershipPricing.vue`
- `src/components/resources/productPage/ProductDocumentation.vue`
- `src/components/resources/productPage/productContent.ts`

**Delete all about page section components:**
- `src/components/resources/aboutPage/TheAboutCompany.vue`
- `src/components/resources/aboutPage/TheAboutTeam.vue`
- `src/components/resources/aboutPage/TheAboutPartners.vue`
- `src/components/resources/aboutPage/TheAboutUpdates.vue`
- `src/components/resources/aboutPage/TheAboutContact.vue`
- `src/components/resources/aboutPage/aboutContent.ts`

**Delete old about page components** (if not already removed):
- `src/components/resources/aboutPage/TheAboutSponsors.vue`
- `src/components/resources/aboutPage/TheAboutHero.vue`
- `src/components/resources/aboutPage/TheAboutTrustPillars.vue`
- `src/components/resources/aboutPage/TheAboutPrinciples.vue`
- `src/components/resources/aboutPage/TheAboutEcosystem.vue`
- `src/components/resources/aboutPage/TheAboutBottomCTA.vue`

**Delete success story components:**
- `src/components/resources/successStories/successStoryContent.ts`
- Any `SuccessStory*.vue` components

**Delete shared components only used by migrated pages** (verify no other imports first):
- `src/components/generics/ProblemSolutionBlock.vue`
- `src/components/generics/AccessibleCarousel.vue`
- `src/components/generics/NewsCard.vue`
- `src/components/generics/LandingPageHeader.vue` (if replaced by Astro header)
- `src/components/generics/LandingPageFooter.vue` (if replaced by Astro footer)

### 4.3 Update Router

**Modify `src/router/index.ts`:**
- Remove routes: `/`, `/about`, `/product`, `/testimonials`, `/newsletter`, `/contact`, `/success-stories/*`
- These routes are now handled by nginx → Astro before reaching the SPA
- Keep all platform routes (`/companies/*`, `/dataprivacy`, `/terms`, `/imprint`, `/pricing`, etc.)

### 4.4 Remove Unused Dependencies

After deleting components, check for:
- Unused imports in remaining files (grep for deleted component names)
- Composables only used by deleted components (e.g. `useContactModal.ts` if only used in marketing pages)
- SCSS files only imported by deleted components

### 4.5 Run SPA CI Gate

```bash
cd dataland-frontend
npm run typecheck          # Must pass — no dangling imports to deleted files
npm run lintci             # Must pass — no unused imports/variables
npm run formatci           # Must pass
npm run checkdependencies  # Must pass — no unused deps
npm run testcomponent      # Must pass — update/remove tests for deleted components
```

**Fix component tests:**
- Delete tests for removed components (e.g. `LandingPage.cy.ts`, `AboutPage.cy.ts`, `PersonCard.cy.ts`)
- Update any remaining tests that imported or referenced deleted components
- Run `npm run testcomponent` and fix all failures

### 4.6 Verify No Dangling References

```bash
# Search for imports of deleted files across the entire SPA
grep -r "LandingPage\|AboutPage\|ProductPage\|TestimonialsPage\|NewsletterPage\|ContactPage" \
  dataland-frontend/src/ --include="*.ts" --include="*.vue" -l

# Search for deleted component names
grep -r "TheIntro\|TheFindLei\|TheWhyUs\|TheTrustedBy\|TheCustomerStories\|TheTestimonials\|TheFrameworks\|TheCustomerProfiles\|TheNewsInsights" \
  dataland-frontend/src/ --include="*.ts" --include="*.vue" -l

# Should return ZERO results
```

---

## Phase 5: Nginx Routing + Integration

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Phase 4 complete (both codebases build independently).

### Tasks

1. Add nginx location rules to `dataland-inbound-proxy` config:
   ```
   = /                          → Astro static HTML
   = /about                     → Astro
   = /product                   → Astro
   = /testimonials              → Astro
   /success-stories/            → Astro
   /_astro/                     → Astro assets (long cache headers)
   everything else              → Vue SPA (fallback)
   ```
2. Add `dataland-website` as Gradle module with node-gradle plugin
3. Build step: `npm run build` → outputs to `dist/`
4. Docker: copy `dist/` into the proxy container or a simple nginx container
5. **Verify routing end-to-end:**
   - Marketing pages (`/`, `/about`, `/product`, `/testimonials`, `/success-stories/*`) serve static HTML
   - Platform routes (`/companies/*`, `/dataprivacy`, `/terms`, `/imprint`) still reach Vue SPA
   - Footer legal links from Astro pages navigate to SPA correctly
   - Login/register links from Astro header reach Keycloak

---

## Phase 6: Screenshot-Based Visual QA

**Dependencies:** Phase 5 complete. Full stack running with Astro + SPA routing.

### Step 6.1 — Human provides screenshots

Start the dev stack and take screenshots at these viewports:

| Viewport | Pages to capture |
|---|---|
| Desktop (1440px+) | Landing (full scroll), Product (full scroll), About (full scroll), Testimonials |
| Tablet (768–1024px) | Landing, Product, About |
| Mobile (< 768px) | Landing (hamburger closed + open), Product, About |
| Small mobile (< 640px) | Landing hero (CTA stacking), Product pricing section |

### Step 6.2 — `ux-designer` — Screenshot Visual QA

**Prompt:**

> I am providing screenshots of the new Dataland Astro website at multiple viewports. The spec is at `dataland-website/SPEC.md`. Compare each screenshot against the spec and the visual layout reference diagrams. Identify layout, spacing, typography, color, responsive, and accessibility issues. For each issue, provide: description, which screenshot, and the specific file + Tailwind classes to fix.

### Step 6.3 — Fix visual issues

1 `frontend-developer` (Agent X) fixes all issues from the visual review, then:

1. Re-run `npm run build` in `dataland-website` — fix any regressions.
2. Re-run SPA CI checks in `dataland-frontend` — confirm nothing regressed.
3. Commit, push, and verify remote CI.

---

## Phase 7: Final CI + Commit

**Agent:** 1 `frontend-developer` (Agent X), serial
**Dependencies:** Phase 6 complete.

### Tasks

1. **Both codebases must build:**
   ```bash
   cd dataland-website && npm run build && npx astro check
   cd dataland-frontend && npm run typecheck && npm run lintci && npm run formatci && npm run checkdependencies && npm run testcomponent
   ```

2. **Commit and push** to `feature/rework-about-page`.

3. **Monitor remote CI:**
   ```bash
   gh run list --branch feature/rework-about-page --limit 1 --json status,conclusion,name
   ```
   If any job fails:
   - Run `gh run view <run-id> --log-failed`
   - Fix locally, push, repeat until green

### Exit Criteria

- `dataland-website`: `npm run build` produces static output, all pages render
- `dataland-frontend`: All CI checks pass (typecheck, lintci, formatci, checkdependencies, testcomponent)
- Remote CI: all green (including E2E tests that require Docker stack)
- No dangling imports, no dead code in either codebase
- All migrated files deleted from `dataland-frontend`

---

## Phase Summary

```
Phase 1  [Build Astro Site]            ████████████████  1 agent (X), serial, 6 sessions
Phase 2  [Independent Reviews]         ██████            2 agents (Y + copywriter), parallel, read-only
Phase 3  [Fix Review Issues]           ████              1 agent (X), serial
Phase 4  [Vue SPA Cleanup]             ████████          1 agent (X), serial — delete migrated files, fix SPA CI
Phase 5  [Nginx Routing]               ████              1 agent (X), serial
Phase 6  [Screenshot QA]               ████████          Human + ux-designer + fixes
Phase 7  [Final CI + Commit]           ██                1 agent (X), serial → all CI must be GREEN
```

**Total agents:** 5 (Agent X builder, Agent Y code reviewer, copywriter, ux-designer, + human for screenshots)
**Parallel worktrees:** 0
**Branches:** 1 (`feature/rework-about-page`)
**CI checkpoints:** Phase 3 (Astro), Phase 4 (SPA), Phase 7 (both + remote)

---

## Key Principles

1. **One branch, no worktrees.** All agents work on `feature/rework-about-page`.
2. **Sequential builds.** Phase 1 runs serially. Each session ends with a passing build.
3. **Parallel reviews are safe.** Phase 2 agents are read-only — they produce issue lists but never edit files.
4. **Builder never reviews its own work.** Agent X builds. Agent Y reviews code. The copywriter reviews text.
5. **Delete after migrate.** Phase 4 removes all migrated code from the SPA. The SPA CI must still pass after deletion.
6. **Spec + Vue content files are the dual source of truth.** SPEC.md for structure/layout. Vue content files for exact copy text.
7. **Verify at every session.** Every build session ends with `npm run build` succeeding — don't accumulate breakage.

---

## Files to Delete from dataland-frontend (Phase 4 Checklist)

This is the complete list. **Before deleting each file, grep to confirm no remaining imports.**

### Page Components
- [ ] `src/components/pages/LandingPage.vue`
- [ ] `src/components/pages/AboutPage.vue`
- [ ] `src/components/pages/ProductPage.vue`
- [ ] `src/components/pages/TestimonialsPage.vue`
- [ ] `src/components/pages/NewsletterPage.vue`
- [ ] `src/components/pages/ContactPage.vue`

### Landing Page Resources
- [ ] `src/components/resources/landingPage/TheIntro.vue`
- [ ] `src/components/resources/landingPage/TheFindLei.vue`
- [ ] `src/components/resources/landingPage/TheWhyUs.vue`
- [ ] `src/components/resources/landingPage/TheTrustedBy.vue`
- [ ] `src/components/resources/landingPage/TheCustomerStories.vue`
- [ ] `src/components/resources/landingPage/TheTestimonials.vue`
- [ ] `src/components/resources/landingPage/TheFrameworks.vue`
- [ ] `src/components/resources/landingPage/TheCustomerProfiles.vue`
- [ ] `src/components/resources/landingPage/TheNewsInsights.vue`
- [ ] `src/components/resources/landingPage/landingContent.ts`
- [ ] `src/components/resources/landingPage/TheQuotes.vue`
- [ ] `src/components/resources/landingPage/TheHowItWorks.vue`
- [ ] `src/components/resources/landingPage/TheJoinCampaign.vue`
- [ ] `src/components/resources/landingPage/TheStruggle.vue`

### Product Page Resources
- [ ] `src/components/resources/productPage/ProductIntro.vue`
- [ ] `src/components/resources/productPage/ProductHowItWorks.vue`
- [ ] `src/components/resources/productPage/ProductGettingData.vue`
- [ ] `src/components/resources/productPage/ProductFeatures.vue`
- [ ] `src/components/resources/productPage/ProductUseCases.vue`
- [ ] `src/components/resources/productPage/ProductCustomerStories.vue`
- [ ] `src/components/resources/productPage/ProductMembershipPricing.vue`
- [ ] `src/components/resources/productPage/ProductDocumentation.vue`
- [ ] `src/components/resources/productPage/productContent.ts`

### About Page Resources
- [ ] `src/components/resources/aboutPage/TheAboutCompany.vue`
- [ ] `src/components/resources/aboutPage/TheAboutTeam.vue`
- [ ] `src/components/resources/aboutPage/TheAboutPartners.vue`
- [ ] `src/components/resources/aboutPage/TheAboutUpdates.vue`
- [ ] `src/components/resources/aboutPage/TheAboutContact.vue`
- [ ] `src/components/resources/aboutPage/aboutContent.ts`
- [ ] `src/components/resources/aboutPage/TheAboutSponsors.vue`
- [ ] `src/components/resources/aboutPage/TheAboutHero.vue`
- [ ] `src/components/resources/aboutPage/TheAboutTrustPillars.vue`
- [ ] `src/components/resources/aboutPage/TheAboutPrinciples.vue`
- [ ] `src/components/resources/aboutPage/TheAboutEcosystem.vue`
- [ ] `src/components/resources/aboutPage/TheAboutBottomCTA.vue`

### Success Stories
- [ ] `src/components/resources/successStories/successStoryContent.ts`
- [ ] Any `SuccessStory*.vue` components

### Shared Components (delete only if unused after above removal)
- [ ] `src/components/generics/ProblemSolutionBlock.vue`
- [ ] `src/components/generics/AccessibleCarousel.vue`
- [ ] `src/components/generics/NewsCard.vue`
- [ ] `src/components/generics/LandingPageHeader.vue`
- [ ] `src/components/generics/LandingPageFooter.vue`

### Tests for Deleted Components
- [ ] `tests/component/components/pages/LandingPage.cy.ts`
- [ ] `tests/component/components/pages/AboutPage.cy.ts`
- [ ] Any other test files referencing deleted components

### Router Routes to Remove
- [ ] `/` (landing)
- [ ] `/about`
- [ ] `/product`
- [ ] `/testimonials`
- [ ] `/newsletter`
- [ ] `/contact`
- [ ] `/success-stories/*`
