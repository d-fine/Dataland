# Homepage Rework -- Implementation Specification

**Date:** 2026-03-18
**Status:** DRAFT -- Under review
**Branch:** `feature/rework-about-page`
**Input:** Valeria's `DL_Homepage_Spec` document, existing `LANDING_ABOUT_REWORK_SPEC.md`, UX designer review

---

## Table of Contents

1. [Design Decisions Log](#1-design-decisions-log)
2. [Global Changes](#2-global-changes)
3. [Landing Page Specification](#3-landing-page-specification)
4. [Product Page Specification](#4-product-page-specification)
5. [About Page Specification](#5-about-page-specification)
6. [Additional Pages](#6-additional-pages)
7. [Content Data](#7-content-data)
8. [Implementation Checklist](#8-implementation-checklist)

---

## Scope

This specification covers **static frontend changes only**. Features requiring backend integration (newsletter subscription, contact form submission, demo request form processing) are documented in a separate backlog file (`HOMEPAGE_REWORK_BACKLOG.md`).

The existing **Company Search API** integration is retained as it is already implemented.

### Pages in Scope

| Page | Route | Status |
|------|-------|--------|
| Landing Page | `/` | REWORK |
| Product Page | `/product` | NEW |
| About Page | `/about` | REWORK |
| Newsletter Page | `/newsletter` | NEW (static layout only) |
| Contact Page | `/contact` | NEW (static layout only) |
| Testimonials Page | `/testimonials` | NEW |

---

## 1. Design Decisions Log

### DECISION 1: Landing Page Section Count

- **Valeria proposed:** 10 sections (Intro, Find LEI, Why Us, Trusted By, Customer Stories, Testimonials, Frameworks, Customer Profiles, News, Footer)
- **UX review flagged:** Page bloat for B2B audience
- **FINAL:** Accept Valeria's 10-section approach. The content-rich landing page is appropriate for this stage of the product — institutional buyers need comprehensive information before committing to a vendor evaluation.

### DECISION 2: Hero Layout

- **Valeria proposed:** Two-column layout (headline + subtext + CTAs on left, illustration on right)
- **Existing spec:** Centered layout with search bar prominent
- **FINAL:** Accept Valeria's two-column hero layout. The company search bar moves to a dedicated "Find LEI" section below the hero. The hero focuses on the value proposition and primary CTAs.

### DECISION 3: Auto-Scrolling Carousels

- **Valeria proposed:** Auto-scrolling carousels for Trusted By logos, Testimonials, and News
- **UX review flagged:** WCAG 2.2.2 violation, reduced engagement with secondary content
- **FINAL:** All carousels must have **pause controls** and respect `prefers-reduced-motion`. On desktop, auto-scroll is enabled by default but can be paused. On mobile, carousels are swipe-only (no auto-scroll). Every carousel must support keyboard navigation (arrow keys) and announce slide changes to screen readers via `aria-live="polite"`.

### DECISION 4: Header Navigation Dropdowns

- **Valeria proposed:** Hover-triggered dropdown menus on "Product" and "About" nav items
- **UX review flagged:** Hover doesn't work on touch devices; creates "hover tunnels"
- **FINAL:** Use **click/tap-triggered dropdowns** instead of hover. This works identically on touch and mouse devices. Dropdowns must be keyboard-accessible (Enter/Space to open, arrow keys to navigate, Escape to close).

### DECISION 5: Breakpoints

- **Valeria proposed:** 2 breakpoints (tablet 1024px, mobile 640px)
- **Existing spec:** 4 breakpoints (640, 768, 1024, 1440)
- **FINAL:** Use **4 breakpoints** from the existing spec. The 768px breakpoint is critical for iPad portrait mode, common in German financial services meeting rooms. The 1440px breakpoint ensures content doesn't stretch excessively on wide monitors.

### DECISION 6: Typography Scale

- **Valeria proposed:** 12px (xs) to 40px (display) — flat scale
- **Existing spec:** 16px (body) to 100px (hero headline)
- **FINAL:** Use a **dramatic scale** for the landing page hero. Hero headline: 64px at xl, scaling down. Section headlines: 32px. Body text: 16px. This creates clear visual hierarchy where the hero dominates and sections are clearly demarcated. The exact scale per section is specified inline.

### DECISION 7: CTA Standardization

- **Valeria proposed:** 4 CTA types (Request a demo, Learn more, Subscribe newsletter, Get in touch)
- **UX review flagged:** Conversion path fragmentation
- **FINAL:** Standardize to **2 primary CTA patterns** across the site:

| CTA | Label | Action | Style |
|-----|-------|--------|-------|
| Primary | `Try it free` | Keycloak register flow | PrimeVue `Button`, `rounded`, orange background |
| Secondary | `Get in touch` | Opens `ContactInquiryModal` (or navigates to `/contact`) | PrimeVue `Button`, `rounded`, `severity="secondary"` |

Where context-specific CTAs are needed (e.g., "Discover platform features", "Explore the Use Cases"), they use the secondary button style and link to the relevant section/page.

### DECISION 8: Color Palette

- **Valeria proposed:** Dark blue `#0f3a82` as primary text color
- **UX review flagged:** Not in existing design system; lower contrast than current near-black
- **FINAL:** Keep the **existing CSS variable system** (`--p-primary-color` for orange, `--p-highlight-color` for headings, grey tones for secondary text). The dark blue (`#0f3a82`) is used **only** for the Team section cards (dark background) where it is already established. Do not introduce it as a global text color.

### DECISION 9: News & LinkedIn Links

- **UX review flagged:** Linking users off-site to LinkedIn is a conversion leak
- **FINAL:** Accept the News & Insights section with LinkedIn links. There is a reliable editorial pipeline for content updates. Links open in a new tab (`target="_blank" rel="noopener noreferrer"`).

### DECISION 10: Accessibility Provisions

- **UX review flagged:** Colleague's spec lacks skip-link, ARIA landmarks, `prefers-reduced-motion`
- **FINAL:** All accessibility work from the existing spec is carried forward and extended to all new pages:
  - Skip-to-content link in header
  - `role="region"` and `aria-labelledby` on every section
  - `id="main-content"` on `<main>` element
  - `prefers-reduced-motion` media query disables all animations/transitions
  - All carousels support keyboard navigation
  - All interactive elements are focusable and operable via keyboard
  - Color contrast ratios meet WCAG AA (4.5:1 for normal text, 3:1 for large text)

### DECISION 11: Product Page Content Density

- **Valeria proposed:** 8 sections including 9 use cases and 3 full customer stories
- **UX review suggested:** Reduce to 5-6 sections, limit use cases to 3-4
- **Copywriter review:** Recommended consolidating to 5 use cases by merging overlapping entries and cutting two
- **FINAL:** Consolidated to **7 use cases** (from Valeria's original 9). Changes:
  - **Merged** "Coverage of Smaller / Non-Listed Companies" (#2) into "Complementing Existing ESG Data Providers" (#1) — SME coverage is the reason for complementing, not a separate use case
  - **Split** validation/audit angle out of #1 into its own use case "Independent Validation and Audit Trail" (#2) — validation as a second source is a distinct buyer scenario
  - **Kept** "Dataland as Primary ESG Data Source" (#3) — this is the ideal usage of Dataland
  - **Merged** "API-Based Integration" (#5) and "UI-Based Data Access" (#6) into "Data Access via Platform and API" — these are delivery modes, not separate use cases
  - **Cut** "Lean Data Provision Without Bundled Features" (#9) — pricing argument, not a workflow; message absorbed into the Membership & Pricing section
  - **Renamed** "Portfolio-Based Data Retrieval" to "Continuous Coverage for Your Portfolio" — members define a set of companies for monitoring, they do not upload portfolio weights
  - **Reordered** to follow buyer journey: complement → validate → primary source → portfolio → on-demand → access → EU Taxonomy
  - All 8 product page sections retained.

### DECISION 12: Backend-Dependent Features

- **Valeria proposed:** Newsletter signup forms, contact/demo request forms, email sending
- **FINAL:** These features are **out of scope** for this specification. Static layout for Newsletter and Contact pages is included, but form submission logic is deferred to the backlog. The existing Company Search API integration is retained.

### DECISION 13: Copy Review

A copywriter review was conducted on all user-facing text. The following changes were applied:

- **Hero subtext:** Replaced "Low cost. Simple interfaces. Lean features." with "Source-based data. Human-verified quality. On-demand sourcing. Structured for regulatory reporting." — leads with differentiators instead of generic claims.
- **Why Us headline:** Shortened from 26 words to 12 ("Common challenges in sustainability data procurement. How Dataland addresses them.").
- **Why Us Problem #1:** Tightened description, removed redundancy.
- **Why Us Problem #3 title:** "Restricted data usage" → "Restrictive licensing terms" — more specific.
- **Why Us CTA #2:** "Explore the Use Cases" → "Explore use cases" — natural casing.
- **Trusted By headline:** "Trusted by companies of different sizes and from different industries" → "Trusted by institutions across sectors and sizes".
- **Testimonials CTA:** "To the video testimonials" → "Watch member testimonials" — natural English.
- **Frameworks headline:** Shortened to "Supported regulatory frameworks" with subheadline.
- **Frameworks bottom text:** Shortened from 39 words, kept member invitation.
- **Customer Profiles headline:** "Dataland combines flexibility and breadth..." → "Who uses Dataland".
- **News headline:** "We are constantly developing and keeping you informed" → "News and insights".
- **Product Intro headline:** Broken into short declarative fragments for scannability.
- **Product How It Works #1:** "User interface access" → "Platform access", "Visual exploration" → "Browse, search, and download".
- **Product Use Case #3 title:** "Primary ESG Data Source" → "Dataland as primary ESG data source".
- **Product Feature Card #6:** "Flexible formats" → "Multi-framework export" — avoids duplication with Card #1 subtitle.
- **Membership Pricing headline:** Shortened to "Membership and pricing" with subheadline.
- **About Company texts:** Removed "democratizing" language; "belongs to" → "is part of".
- **Footer description:** "Manual data sourcing. Data from source." → "Source-based. Human-verified." — aligns with AI extraction messaging.
- **Newsletter text:** Removed "We keep our communication focused and relevant".
- **Product Use Cases:** Consolidated from 9 to 7. Merged SME coverage into complementing use case, split validation into its own use case, merged API+UI access, cut "lean data provision" (absorbed into pricing section), kept "primary source" as ideal usage, renamed portfolio use case, reordered by buyer journey.

---

## 2. Global Changes

### 2.1 Responsive Breakpoint Variables

**File:** `src/assets/scss/breakpoints.scss`

```scss
$bp-sm: 640px;
$bp-md: 768px;
$bp-lg: 1024px;
$bp-xl: 1440px;
```

**Vite config:** Add to `css.preprocessorOptions.scss.additionalData` so it is available in all components without explicit import:

```ts
css: {
  preprocessorOptions: {
    scss: {
      additionalData: `@use "@/assets/scss/breakpoints" as *;\n`,
    },
  },
},
```

### 2.2 Shared useBreakpoint Composable

**File:** `src/composables/useBreakpoint.ts`

```ts
import { ref, onMounted, onUnmounted, readonly } from 'vue';

export type BreakpointKey = 'sm' | 'md' | 'lg' | 'xl';

const BP_VALUES: Record<BreakpointKey, number> = {
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1440,
};

export function useBreakpoint() {
  const width = ref(globalThis.innerWidth ?? 1440);

  const onResize = (): void => {
    width.value = globalThis.innerWidth;
  };

  onMounted(() => {
    globalThis.addEventListener('resize', onResize);
    onResize();
  });

  onUnmounted(() => {
    globalThis.removeEventListener('resize', onResize);
  });

  const isBelow = (bp: BreakpointKey): boolean => width.value < BP_VALUES[bp];
  const isAbove = (bp: BreakpointKey): boolean => width.value >= BP_VALUES[bp];

  return {
    width: readonly(width),
    isBelow,
    isAbove,
    isMobile: (): boolean => isBelow('md'),
    isTablet: (): boolean => isAbove('md') && isBelow('lg'),
    isDesktop: (): boolean => isAbove('lg'),
  };
}
```

### 2.3 Header Navigation

**File to modify:** `src/components/generics/LandingPageHeader.vue`

The header is a sticky top navigation bar shared across all pages.

**Desktop layout (>= $bp-lg):**

```
[Logo]          [Product ▾]  [About ▾]          [Login] [Try it free]
```

- Logo links to `/`
- "Product" and "About" are **click-triggered dropdown menus** (not hover)
- "Login" is a text link to Keycloak login
- "Try it free" is a primary CTA button linking to Keycloak registration

**Product dropdown items:**

| Label | Link |
|-------|------|
| How it works | `/product#how-it-works` |
| Features | `/product#features` |
| Frameworks | `/product#frameworks` |
| Use Cases | `/product#use-cases` |
| Customer Stories | `/product#customer-stories` |
| Membership & Pricing | `/product#membership-pricing` |
| Documentation | `/product#documentation` |

**About dropdown items:**

| Label | Link |
|-------|------|
| Company | `/about#company` |
| Partners | `/about#partners` |
| News and Insights | `/about#updates` |
| Contact | `/about#contact` |

**Mobile layout (< $bp-lg):**

- Show logo + hamburger icon button
- Clicking hamburger opens a slide-down overlay containing all navigation items (Product subsection, About subsection, Login, Try it free)
- Overlay: `background: rgba(255, 255, 255, 0.96); backdrop-filter: blur(16px);`
- Clicking any link or close icon (pi-times) closes the overlay

**Header properties:**

```
height: 72px
background: var(--p-surface-0, #f7f7f5)
border-bottom: 1px solid var(--p-surface-200, #e6e6e6)
position: sticky
top: 0
z-index: 100
padding: 0 32px
```

**Dropdown behavior:**

- Click/tap on "Product" or "About" toggles the dropdown
- Clicking outside or pressing Escape closes the dropdown
- Arrow keys navigate dropdown items
- Enter/Space activates a dropdown item
- `aria-expanded` and `aria-controls` attributes on trigger buttons
- `role="menu"` on dropdown container, `role="menuitem"` on items

### 2.4 Skip-to-Content Link

Add to `LandingPageHeader.vue` as first child of `<header>`:

```html
<a href="#main-content" class="skip-link">Skip to content</a>
```

```scss
.skip-link {
  position: absolute;
  left: -9999px;
  z-index: 999;
  padding: 0.5rem 1rem;
  background: var(--p-primary-color);
  color: #fff;
  font-weight: 600;
  text-decoration: none;
  &:focus {
    left: 1rem;
    top: 1rem;
  }
}
```

Add `id="main-content"` to the `<main>` element on every page.

### 2.5 Footer

**File:** `src/components/generics/LandingPageFooter.vue` (MODIFY or CREATE)

The footer is shared across all pages. It has two areas: a four-column top section and a single-row bottom bar.

**Top section (4 columns on desktop):**

| Column 1: Dataland | Column 2: Product | Column 3: Company | Column 4: Resources |
|---------------------|--------------------|--------------------|---------------------|
| "Non-profit sustainability data platform. Source-based. Human-verified. Open source." | How it works | Why Dataland | Tutorials |
| [Wertestiftung logo] [d-fine logo] [PwC logo] | Features | About us | Platform documentation |
| | Frameworks | Updates and Insights | Technical Hub |
| | Use Cases | Partners | |
| | Customer Stories | Contact | |
| | Testimonials | | |
| | Membership & Pricing | | |

**Bottom bar:**

```
[Legal]  [Imprint]  [Data Privacy]  [Cookie Settings]    © 2026 Dataland    [LinkedIn icon]
```

**Responsive behavior:**

- Desktop (>= $bp-lg): 4-column grid
- Tablet ($bp-md to $bp-lg): 2x2 grid
- Mobile (< $bp-md): Single column stack

**Style:**

```scss
.footer {
  background: var(--p-surface-0, #f7f7f5);
  border-top: 1px solid var(--p-surface-200, #e6e6e6);
  padding: 64px 64px 32px;

  &__top {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    max-width: 1200px;
    margin: 0 auto;
  }

  &__col-title {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0 0 16px;
  }

  &__link {
    font-size: 0.875rem;
    color: var(--p-text-color);
    text-decoration: none;
    display: block;
    padding: 4px 0;
    &:hover {
      color: var(--p-primary-color);
    }
  }

  &__bottom {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 24px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid var(--p-surface-200, #e6e6e6);
    font-size: 0.875rem;
    color: var(--p-text-muted-color);
  }
}

@media only screen and (max-width: $bp-lg) {
  .footer__top {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .footer {
    padding: 32px 16px 24px;
  }
  .footer__top {
    grid-template-columns: 1fr;
  }
  .footer__bottom {
    flex-wrap: wrap;
    gap: 12px;
  }
}
```

**Footer link targets:**

| Link | Target |
|------|--------|
| Product links | `/product#<anchor>` |
| Why Dataland | `/#why-us` |
| About us | `/about#company` |
| Updates and Insights | `/about#updates` |
| Partners | `/about#partners` |
| Contact | `/about#contact` |
| Tutorials | `/product#documentation` |
| Platform documentation | `/product#documentation` |
| Technical Hub | `/product#documentation` |
| Legal | `/legal` (existing) |
| Imprint | `/imprint` (existing) |
| Data Privacy | `/dataprivacy` (existing) |
| Cookie Settings | `/cookies` (existing) |
| LinkedIn | `https://www.linkedin.com/company/dataland-gmbh` (new tab) |

### 2.6 Reduced Motion Support

Add a global SCSS rule:

```scss
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

---

## 3. Landing Page Specification

### 3.1 Section Order

```
1. TheIntro              (Hero — two-column)
2. TheFindLei            (Company search)
3. TheWhyUs              (Problem-solution pairs)
4. TheTrustedBy          (Logo carousel)
5. TheCustomerStories    (Story cards)
6. TheTestimonials       (Quote carousel)
7. TheFrameworks         (Framework cards)
8. TheCustomerProfiles   (Sector mosaic)
9. TheNewsInsights       (News slider)
10. Footer               (Shared component)
```

### 3.2 LandingPage.vue Template

**File:** `src/components/pages/LandingPage.vue`

```html
<template>
  <main id="main-content" role="main">
    <TheIntro />
    <TheFindLei />
    <TheWhyUs />
    <TheTrustedBy />
    <TheCustomerStories />
    <TheTestimonials />
    <TheFrameworks />
    <TheCustomerProfiles />
    <TheNewsInsights />
    <ContactInquiryModal />
  </main>
</template>
```

---

### 3.3 Section: TheIntro (Hero)

**File:** `src/components/resources/landingPage/TheIntro.vue` (MODIFY)

#### Content

- **Headline:** `Democratizing access to high-quality sustainability data:` (line break) `A European non-profit shared data platform`
- **Subtext:** `Source-based data. Human-verified quality. On-demand sourcing. Structured for regulatory reporting.`
- **CTA left:** `Try it free` (primary — Keycloak register)
- **CTA right:** `Get in touch` (secondary — ContactInquiryModal or `/contact`)
- **Right column:** Illustration image (`intro_art.svg`)

#### Layout

Two-column grid: 60% left (text), 40% right (illustration).

- Desktop (>= $bp-xl): `grid-template-columns: 60% 40%`. Headline: 48px bold. Subtext: 20px. Padding: 80px 64px. Min-height: 560px.
- Large (>= $bp-lg): Headline: 40px. Padding: 64px.
- Tablet ($bp-md to $bp-lg): Stack vertically (illustration below text). Headline: 36px.
- Mobile (< $bp-md): Stack vertically. Headline: 32px. Padding: 40px 16px. CTAs stack vertically.

#### Template

```html
<section class="intro" role="region" aria-label="Introduction">
  <div class="intro__content">
    <h1 class="intro__headline">
      Democratizing access to high-quality sustainability data:<br />
      <span class="intro__headline--highlight">A European non-profit shared data platform</span>
    </h1>
    <p class="intro__subtext">
      Source-based data. Human-verified quality. On-demand sourcing. Structured for regulatory reporting.
    </p>
    <div class="intro__actions">
      <Button label="Try it free" rounded @click="handleRegister" />
      <Button label="Get in touch" rounded severity="secondary" @click="openModal" />
    </div>
  </div>
  <div class="intro__illustration">
    <img src="/static/images/intro_art.svg" alt="Dataland platform illustration" />
  </div>
</section>
```

---

### 3.4 Section: TheFindLei (Company Search)

**File:** `src/components/resources/landingPage/TheFindLei.vue` (CREATE)

#### Content

- **Headline:** `Search sustainability data by company name or identifier`
- **Search bar:** Reuses existing `CompaniesOnlySearchBar` component
- **Placeholder text:** `Search sustainability data by company name or identifier (e.g., LEI, ISIN, etc.)`

#### Behavior

- Uses the existing Company Search API (`GET /api/companies/names`)
- Autocomplete suggestions after 4 characters
- Selecting a company navigates to the company page
- Pressing Enter with text navigates to `/companies?input=<query>`

#### Layout

- Centered, max-width 520px for search bar
- Padding: 64px
- Background: white

---

### 3.5 Section: TheWhyUs (Problem-Solution)

**File:** `src/components/resources/landingPage/TheWhyUs.vue` (CREATE)

#### Content

- **Headline:** `Common challenges in sustainability data procurement. How Dataland addresses them.`

**Problem-solution pairs (each rendered as two columns with a central arrow):**

| # | Problem | Solution |
|---|---------|----------|
| 1 | **Missing issuer data** — Large ESG data providers typically focus on listed companies, leaving smaller, regional, or unlisted issuers outside their standard coverage. Data consumers must then identify, source, and structure the missing data themselves. | **Data on demand** — Dataland provides the data its members actually need. If a required dataset is missing, members can request it. The data will be sourced from issuer disclosures and added to the platform, so gaps in coverage can be addressed when they arise |
| 2 | **Poor data quality** — Many data sourcing approaches introduce errors, inconsistencies, outdated values, or unexplained gaps. Inaccurate or untraceable ESG data undermines reporting, analytics, and decision-making | **AI extraction with human verification and full source  traceability** — Dataland sources data directly from the original publisher and combines tailored AI extraction with manual verification steps. Every published data point is linked to its original source document, ensuring full traceability. This means datasets are not only structured efficiently and quality-assured by humans, but also independently verifiable at any time |
| 3 | **Restrictive licensing terms** — Acquired datasets are often subject to restrictive usage rights, limiting how they can be applied across reporting, analysis, validation, and other internal workflows. This reduces the practical value of the data far beyond the original use case | **Unrestricted use** — Dataland data can be used freely and published freely. This allows the same dataset to support multiple teams and workflows without unnecessary licensing constraints |
| 4 | **High prices** — Many providers offer expensive data packages that are not well aligned with the actual needs of the data consumer. Institutions often end up paying for broad coverage, bundled content, or additional functionality that is irrelevant to their use case | **Lean pricing model** — Dataland follows a shared procurement model in which pricing reflects the effort required to source a dataset. The costs of that sourcing effort are shared across the members who need the data rather than being borne by each institution individually |

**CTA buttons (3 across):**

| Button | Label | Link |
|--------|-------|------|
| 1 | Discover platform features | `/product#features` |
| 2 | Explore use cases | `/product#use-cases` |
| 3 | Get in touch | `/about#contact` |

#### Problem-Solution Element Design

Each pair is rendered as a 3-column grid: `40% | 20% | 40%`.

- Left column: Problem title (bold, 20px) + description text (16px, muted)
- Center: Arrow SVG image (`arrow_big.svg`), centered vertically, `aria-hidden="true"`
- Right column: Solution title (bold, 20px, orange accent) + description text (16px, muted)

**Responsive:**

- Desktop (>= $bp-lg): 3-column grid as described
- Tablet ($bp-md to $bp-lg): Stack vertically (problem, then arrow rotated 90°, then solution)
- Mobile (< $bp-md): Stack vertically, arrow hidden, problem and solution separated by a divider line

#### Layout

- Background: white
- Padding: 80px 64px
- Headline: 32px bold, centered, max-width 900px
- Problem-solution blocks: max-width 1100px, centered, 24px gap between blocks
- CTA buttons: 3-column grid (30% each, 5% gaps), centered below

---

### 3.6 Section: TheTrustedBy (Logo Carousel)

**File:** `src/components/resources/landingPage/TheTrustedBy.vue` (CREATE)

#### Content

- **Headline:** `Trusted by institutions across sectors and sizes`
- **Logo carousel:** Auto-scrolling row of member/partner logos

**Confirmed logos (from Valeria's spec):**

Atlas Metrics, Bantleon, BayernInvest, BayernLB, BVI, EuroDat, Laiqon, Deutsche Rück, d-fine, Hansa-Invest, NORD/LB, PwC, T-Systems, Werte-Stiftung

All logos link to `/product#customer-stories`.

#### Carousel Behavior (per Decision 3)

- Auto-scrolls left, 4 visible items at a time, 7s per slide, loops
- **Pause button** visible (icon: `pi pi-pause` / `pi pi-play`)
- Respects `prefers-reduced-motion` — if enabled, carousel is static (no auto-scroll)
- Keyboard: arrow keys navigate, Tab moves to pause button
- `aria-live="polite"` on slide container
- Touch: swipe-only on mobile (no auto-scroll)

**Responsive visible items:**

- Desktop (>= $bp-lg): 4 items
- Tablet ($bp-md to $bp-lg): 2 items
- Mobile (< $bp-md): 1 item

#### Layout

- Background: white
- Padding: 80px 64px
- Headline: 32px bold, centered

---

### 3.7 Section: TheCustomerStories (Story Cards)

**File:** `src/components/resources/landingPage/TheCustomerStories.vue` (CREATE)

#### Content

- **Headline:** `Customer stories`
- **3 story cards** in a row:

| # | Logo | Tag | Text | Link |
|---|------|-----|------|------|
| 1 | MEAG logo | Asset Manager | Filling SFDR gaps and EU Taxo template transition | `/product#meag` |
| 2 | NORD/LB logo | Bank | Primary source of ESG data and API integration | `/product#nordlb` |
| 3 | ÖV Braunschweig logo | Insurance | PAI lineage and source transparency for compliance | `/product#ovbraunschweig` |

#### Card Design

```scss
.story-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;

  &__logo {
    height: 60px;
    object-fit: contain;
  }

  &__tag {
    font-size: 0.875rem;
    color: var(--p-primary-color);
    background: var(--p-primary-50, #fff3e6);
    padding: 4px 12px;
    border-radius: 999px;
    display: inline-block;
    width: fit-content;
  }

  &__text {
    font-size: 1rem;
    line-height: 1.5;
  }

  &__link {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--p-primary-color);
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}
```

#### Layout

- Background: white
- 3-column grid (30% each, 5% gaps)
- Tablet: 2 columns (third wraps)
- Mobile: 1 column

---

### 3.8 Section: TheTestimonials (Quote Carousel)

**File:** `src/components/resources/landingPage/TheTestimonials.vue` (CREATE)

#### Content

- **Headline:** `What our members share about their experience`
- **Carousel of testimonial cards** (text quotes, no video embeds)
- **CTA button:** `Watch member testimonials` → `/testimonials`

**Testimonials data (12 quotes):**

| # | Author | Affiliation | Quote |
|---|--------|-------------|-------|
| 1 | Stephen Henkel | Managing Director at VÖB-Service GmbH | "Then came Dataland with the idea that ESG data should be a common good, and I think that's excellent" |
| 2 | Jasmina Klein | Manager at d-fine | "Dataland will help to let the data flow and this is the key to solve one of the most pressing issues of our time" |
| 3 | Matthias Kopp | Director of Sustainable Finance at WWF Germany | "Dataland can provide the data ecosystem we all need to support our transition to stay within 1.5 degrees or within the planetary boundaries" |
| 4 | Christian Heller | CEO of Value Balancing Alliance | "Join Dataland, share your data, and make use of it to transform the economy into a just and sustainable one" |
| 5 | Fabian Kloss | Cloud Services Sales at T-Systems International | "Have a look at Dataland and look at what benefits you can get out of it for your business. Join the community!" |
| 6 | Ingo Speich | Head of Sustainability & Corporate Governance at Deka Investment | "We appeal to both investors and companies to make sustainability data available in a timely and cost-efficient manner" |
| 7 | Christoph Benner | CEO of Chom Capital | "Our partnership with Dataland is instrumental in progressively addressing and bridging data gaps" |
| 8 | Rudolf Siebel | Managing Director at BVI German Fund Association | "Through Dataland, we hope that data availability, coverage and quality is improved, for the benefit of the users, the corporations and society overall" |
| 9 | Dr. Annalisa Schwarz | Managing Director at Werte-Stiftung | "Dataland will help solve data issues by ensuring transparent, open and fair access to sustainability data" |
| 10 | Daniel Sailer | Head of Sustainable Investment Office at Metzler Asset Management GmbH | "The Pathways to Paris PoC can be a simple way to make sure Dataland becomes the data platform you need" |
| 11 | Dr. Egbert Schark | Founder and Managing Director at d-fine GmbH | "I believe the fascinating idea is worth supporting. Join the mission! Join Dataland!" |
| 12 | Sven Schuchert | CEO of Envoria | "Dataland is the only platform we know that is open to everyone and based on a non-profit business model" |

#### Testimonial Card Design

```scss
.testimonial-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 12px;
  padding: 24px;

  &__quote {
    font-size: 1.25rem;
    font-style: italic;
    line-height: 1.5;
    margin: 0 0 16px;
  }

  &__author {
    font-size: 0.875rem;
    font-weight: 700;
    text-align: right;
  }

  &__affiliation {
    font-size: 0.875rem;
    color: var(--p-text-muted-color);
    text-align: right;
  }
}
```

#### Carousel Behavior

Same as TheTrustedBy (Decision 3): auto-scroll with pause control, keyboard navigation, `prefers-reduced-motion` support. 4 visible items on desktop, 2 on tablet, 1 on mobile.

---

### 3.9 Section: TheFrameworks (Framework Cards)

**File:** `src/components/resources/landingPage/TheFrameworks.vue` (CREATE)

#### Content

- **Headline:** `Supported regulatory frameworks`
- **Subheadline:** `Structured data for the frameworks that drive European sustainability reporting.`

**Framework cards (6):**

| # | Title | Subtitle | Description |
|---|-------|----------|-------------|
| 1 | EU Taxonomy | Financials | The EU Taxonomy Regulation enables financial institutions to assess and report the share of environmentally sustainable economic activities within their portfolios, based on eligibility and alignment metrics |
| 2 | EU Taxonomy | Non-Financials | The EU Taxonomy Regulation provides a framework for non-financial companies to disclose the extent to which their activities are environmentally sustainable, based on defined technical screening criteria |
| 3 | EU Taxonomy | Nuclear and Gas | The EU Taxonomy includes specific criteria for nuclear and gas activities under transitional provisions, allowing companies to report their contribution to climate objectives under defined conditions |
| 4 | SFDR | | The Sustainable Finance Disclosure Regulation requires financial market participants to disclose how sustainability risks are integrated into investment decisions and to report Principal Adverse Impact indicators at entity and product level |
| 5 | PCAF | | The PCAF standard provides a methodology for financial institutions to measure and disclose financed emissions associated with their lending and investment portfolios |
| 6 | LkSG | | Lieferkettensorgfaltspflichtengesetz is a German law requiring companies to identify, assess, and manage human rights and environmental risks within their supply chains |

All cards link to `/product#frameworks`.

**Bottom text:** `New frameworks, datasets, and features are added based on member input and regulatory developments. Become a member to help shape what comes next.`

**CTA buttons:**

| Button | Label | Link |
|--------|-------|------|
| 1 | News & insights | `/about#updates` |
| 2 | Subscribe to our Newsletter | `/newsletter` |

#### Framework Card Design

Each card has a subtle geometric orange accent in the top-right corner.

```scss
.framework-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 40px;
    height: 40px;
    background: var(--p-primary-color);
    opacity: 0.15;
    clip-path: polygon(100% 0, 0 0, 100% 100%);
  }

  &__title {
    font-size: 1.25rem;
    font-weight: 700;
  }

  &__subtitle {
    font-size: 1.25rem;
    color: var(--p-text-muted-color);
  }

  &__text {
    font-size: 0.875rem;
    line-height: 1.5;
    margin-top: 12px;
  }
}
```

#### Layout

- 3x2 grid on desktop
- 2x3 grid on tablet
- 1 column on mobile

---

### 3.10 Section: TheCustomerProfiles (Sector Mosaic)

**File:** `src/components/resources/landingPage/TheCustomerProfiles.vue` (CREATE)

#### Content

- **Headline:** `Who uses Dataland`

**Sector tiles (11 total) in a weighted mosaic layout:**

| Tile | Icon | Size |
|------|------|------|
| Banks | `icon_bank.svg` | XL |
| Insurance companies | `icon_insurance.svg` | XL |
| Asset Managers | `icon_asset_manager.svg` | XL |
| Pension funds | `icon_pension.svg` | L |
| Public Financial Institutions | `icon_public_fin.svg` | L |
| Data Providers | `icon_vendors.svg` | M |
| Financial Data Infrastructure | `icon_fin_data.svg` | S |
| ESG solution providers | `icon_esg_software.svg` | S |
| Industry Associations | `icon_industry.svg` | S |
| Sustainability Initiatives | `icon_esg_org.svg` | S |
| Academic Institutions | `icon_academy.svg` | S |

Size factor determines the visual weight (tile dimensions) in the mosaic grid.

#### Tile Design

Each tile shows a pictogram icon above a sector title. Larger tiles have a subtle background gradient.

```scss
.sector-tile {
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;

  &__icon {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }

  &__title {
    font-size: 0.875rem;
    font-weight: 600;
  }
}
```

#### Mosaic Grid Layout

On desktop, uses CSS Grid with a 12-column base:

- Row 1 (XL tiles): Banks (cols 1-4), Insurance (cols 5-8), Asset Managers (cols 9-12)
- Row 2 (L tiles): Pension (cols 1-4), Public Fin (cols 5-8), Providers (cols 9-12)
- Row 3 (S tiles): Fin Data (cols 1-3), ESG Software (cols 4-6), Industry (cols 7-9), Sust. Initiatives (cols 10-11), Academic (col 12)

**Responsive:**

- Tablet: 3-column grid, uniform tile sizes
- Mobile: 2-column grid, uniform tile sizes

---

### 3.11 Section: TheNewsInsights (News Slider)

**File:** `src/components/resources/landingPage/TheNewsInsights.vue` (CREATE)

#### Content

- **Headline:** `News and insights`

**News items (slider, 3 visible at a time):**

| # | Image | Title | Date | Link |
|---|-------|-------|------|------|
| 1 | `news_eu_taxo.png` | Smooth transition to the new EU Taxonomy template | March 5, 2026 | LinkedIn post |
| 2 | `news_bvi_fok.png` | Networking at BVI FOK | February 25, 2026 | LinkedIn post |
| 3 | `news_dmm_q12026.png` | Dataland Members' Meeting Q1 2026 | February 20, 2026 | LinkedIn post |
| 4 | `news_2025.png` | 2025 in numbers | January 21, 2026 | LinkedIn post |
| 5 | `news_sfdr2.png` | How SFDR 2.0 reinforces the need for shared ESG data infrastructure | December 10, 2025 | LinkedIn post |
| 6 | `news_pcaf.png` | PCAF on Dataland | November 21, 2025 | LinkedIn post |
| 7 | `news_sust2025.png` | Dataland @ Sustainability Kongress 2025 | November 14, 2025 | LinkedIn post |
| 8 | `news_dmm_q42025.png` | Dataland Members' Meeting Q4 2025 | November 6, 2025 | LinkedIn post |
| 9 | `news_erik.png` | Leadership transition: thank you, Erik Breen! | November 4, 2025 | LinkedIn post |

All links open in new tab (`target="_blank" rel="noopener noreferrer"`).

**Bottom text:** `Want to stay up to date?`

**CTA buttons:**

| Button | Label | Link |
|--------|-------|------|
| 1 | Follow us on LinkedIn | `https://www.linkedin.com/company/dataland-gmbh` (new tab) |
| 2 | Subscribe to our Newsletter | `/newsletter` |

#### News Card Design

```scss
.news-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  overflow: hidden;

  &__image {
    width: 100%;
    height: 220px;
    object-fit: cover;
  }

  &__body {
    padding: 16px;
  }

  &__title {
    font-size: 1.25rem;
    font-weight: 700;
    line-height: 1.4;
  }

  &__date {
    font-size: 0.875rem;
    color: var(--p-text-muted-color);
    margin-top: 8px;
  }

  &__link {
    font-size: 0.875rem;
    color: var(--p-primary-color);
    font-weight: 600;
    margin-top: 8px;
    display: inline-block;
  }
}
```

#### Slider Behavior

Same carousel rules as Decision 3. Navigation arrows (left/right). 3 visible on desktop, 2 on tablet, 1 on mobile.

---

## 4. Product Page Specification

### 4.1 Section Order

```
1. ProductIntro                (Centered headline)
2. ProductHowItWorks           (Integration types)
3. ProductGettingData          (Access paths)
4. ProductFeatures             (Feature cards)
5. ProductUseCases             (9 use case blocks)
6. ProductCustomerStories      (3 detailed stories — MEAG, NORD/LB, ÖVB)
7. ProductMembershipPricing    (Pricing card + credits visual)
8. ProductDocumentation        (API doc links)
```

### 4.2 Route Registration

**File:** `src/router/index.ts`

```ts
{
  path: '/product',
  name: 'product',
  component: () => import('@/components/pages/ProductPage.vue'),
  meta: { layout: 'landing' },
}
```

### 4.3 ProductPage.vue Template

**File:** `src/components/pages/ProductPage.vue` (CREATE)

```html
<template>
  <main id="main-content" role="main">
    <ProductIntro />
    <ProductHowItWorks />
    <ProductGettingData />
    <ProductFeatures />
    <ProductUseCases />
    <ProductCustomerStories />
    <ProductMembershipPricing />
    <ProductDocumentation />
    <ContactInquiryModal />
  </main>
</template>
```

---

### 4.4 Section: ProductIntro

**File:** `src/components/resources/productPage/ProductIntro.vue` (CREATE)

- **Headline:** `Structured sustainability data. Procured on demand. Quality-assured. Traceable to the original source.`
- Centered, 32px bold, max-width 900px
- Padding: 80px 64px

---

### 4.5 Section: ProductHowItWorks

**File:** `src/components/resources/productPage/ProductHowItWorks.vue` (CREATE)

- **Headline:** `How it works`
- **Anchor:** `#how-it-works`

**3 problem-solution blocks (same arrow pattern as landing page TheWhyUs):**

| # | Left Title | Left Text | Right Title | Right Text |
|---|-----------|-----------|-------------|------------|
| 1 | Platform access | Access ESG datasets directly through the Dataland platform and download them in structured formats | Browse, search, and download | Browse companies, portfolios and datasets interactively |
| 2 | API integration | Integrate Dataland data into your internal systems and analytics pipelines | Automated workflows | Retrieve ESG datasets programmatically through stable APIs |
| 3 | Partner integration | Access Dataland data through software partners and ESG data platforms | Embedded data services | Partners integrate Dataland datasets into their own solutions |

---

### 4.6 Section: ProductGettingData

**File:** `src/components/resources/productPage/ProductGettingData.vue` (CREATE)

- **Headline:** `Accessing datasets as a member`
- **Anchor:** `#getting-data`

**2 problem-solution blocks:**

| # | Left Title | Left Text | Right Title | Right Text |
|---|-----------|-----------|-------------|------------|
| 1 | Dataset already available | Use the dataset within the platform or download it for your internal applications | No additional cost | Members can access existing datasets without any delay and free of charge |
| 2 | Dataset not yet available | Request the dataset through the platform | Delivered within one month | Costs are shared between members requesting the same dataset |

---

### 4.7 Section: ProductFeatures

**File:** `src/components/resources/productPage/ProductFeatures.vue` (CREATE)

- **Headline:** `Platform features`
- **Anchor:** `#features`

**6 feature cards (3x2 grid), using framework card style:**

| # | Title | Subtitle | Text |
|---|-------|----------|------|
| 1 | Download data | Flexible formats | Download datasets as CSV or XLSX with or without metadata |
| 2 | Portfolio management | Create and manage portfolios | Build your own portfolios of companies and monitor ESG data availability |
| 3 | Portfolio sharing | Collaboration | Share company portfolios with colleagues and teams |
| 4 | Request data | On-demand sourcing | Order missing datasets directly from the platform |
| 5 | Source transparency | Traceability | Inspect original source documents and quality comments |
| 6 | Multi-framework export | Reporting-ready formats | Export datasets in formats suitable for different reporting frameworks |

---

### 4.8 Section: ProductUseCases

**File:** `src/components/resources/productPage/ProductUseCases.vue` (CREATE)

- **Headline:** `Use Cases`
- **Anchor:** `#use-cases`

**7 use case blocks (problem-solution arrow pattern):**

| # | Title | Description |
|---|-------|-------------|
| 1 | Complementing Existing ESG Data Providers | Dataland complements a primary ESG data provider by closing remaining data gaps. Missing indicators or uncovered companies can be retrieved where the primary provider is incomplete. This includes access to ESG data for SMEs and private companies that are typically not covered by large commercial vendors, extending ESG analysis beyond listed entities and enabling broader coverage of real-economy exposures, especially in lending, private markets, and insurance portfolios |
| 2 | Independent Validation and Audit Trail | Dataland datasets serve as an additional reference point to cross-check consistency, plausibility, and methodological differences against a primary provider — particularly in contexts requiring high data quality and auditability. Every data point is linked to its exact location in the original source document, providing the source transparency that regulatory audits increasingly require |
| 3 | Dataland as Primary ESG Data Source | Dataland serves as the main source of ESG datasets, with data retrieval, reporting, and analysis processes built directly on its datasets. Reliance on traditional ESG data vendors can be reduced or eliminated |
| 4 | Continuous Coverage for Your Portfolio | Retrieval of ESG datasets for defined portfolios (e.g. loan books or investment portfolios), combined with continuous identification of newly available data. Portfolio coverage remains up to date as holdings evolve and additional datasets become available |
| 5 | Targeted Sourcing of Missing Datasets | Missing datasets for specific companies or indicators can be ordered via credits, driven by concrete internal or regulatory requirements. This enables precise data procurement without dependency on predefined data packages |
| 6 | Data Access via Platform and API | Access ESG datasets directly through the Dataland platform to search, retrieve, and download datasets for individual companies — suitable for ad-hoc analysis and manual workflows. For automated pipelines, integrate ESG datasets into internal IT systems (e.g. risk engines, reporting tools, data platforms) via API, supporting automated data ingestion and seamless use within existing system landscapes |
| 7 | EU Taxonomy Template Updates and Format Continuity | Provision of EU Taxonomy datasets in both current and previous template formats, including automated format conversion. Ensures continuity in internal reporting processes when regulatory templates change |

**Note:** Use case blocks use only title + text (no right-column solution), presented as alternating left/right feature blocks with accompanying illustration placeholders.

---

### 4.9 Section: ProductCustomerStories

**File:** `src/components/resources/productPage/ProductCustomerStories.vue` (CREATE)

- **Headline:** `Customer stories`
- **Subheadline:** `Selected examples of how different institutions use Dataland`
- **Anchor:** `#customer-stories`

**3 detailed customer story cards:**

Each card has: customer logo (left, 24% width) + story content (right, 76% width) with tag, title, summary, challenge, solution, value, and quote sections.

#### Story 1: MEAG (Anchor: `#meag`)

- **Tag:** Asset Manager
- **Title:** Closing SFDR Data Gaps and Simplifying the EU Taxonomy Template Transition
- **Summary:** As an asset manager with extensive SFDR reporting obligations, MEAG requires reliable ESG indicators across a large universe of portfolio companies. The firm uses Dataland to close specific data gaps that arise in the datasets delivered by its primary ESG data provider. At the same time, MEAG expects Dataland's EU Taxonomy template conversion capability to simplify the upcoming transition to the revised reporting template.
- **Challenge:** For SFDR reporting, MEAG must compile a range of sustainability indicators across a large universe of portfolio companies. While the firm's main ESG data provider covers most of the required data, some indicators needed for PAI calculations are not always included in the delivered datasets. These gaps create operational friction for the ESG reporting team. In parallel, MEAG faces another operational challenge related to the transition to the revised EU Taxonomy reporting template.
- **Solution:** MEAG uses Dataland as a targeted data source to close specific SFDR data gaps. When indicators required for PAI reporting are missing from the firm's primary dataset, the reporting team retrieves the relevant ESG indicators from Dataland. At the same time, Dataland's EU Taxonomy template conversion functionality will ensure that datasets are available in both template structures.
- **Value:** Using Dataland allows MEAG to resolve two operational challenges within its ESG reporting processes. First, the platform provides a practical way to fill SFDR data gaps. Second, the EU Taxonomy template conversion will simplify the transition to the new reporting format.
- **Quote:** "We initially joined Dataland to close specific SFDR data gaps in our reporting. The upcoming EU Taxonomy template conversion is another strong advantage." — **Dr. Arnd Pauwels**, Head of ESG Reporting

#### Story 2: NORD/LB (Anchor: `#nordlb`)

- **Tag:** Bank
- **Title:** Dataland as a Primary ESG Data Source with Automated Delivery
- **Summary:** NORD/LB uses ESG indicators across several regulatory and internal reporting processes. The bank selected Dataland as its primary ESG data source because it provides high-quality, disclosure-based indicators at a competitive price while allowing the bank to retrieve only the specific datasets required for its reporting workflows.
- **Challenge:** For regulatory frameworks such as SFDR and EU Taxonomy, NORD/LB must compile sustainability indicators for a broad range of corporate counterparties. Traditional ESG data providers typically offer large data packages. When NORD/LB began using Dataland, datasets were initially retrieved manually.
- **Solution:** NORD/LB adopted Dataland as its primary ESG data source. The bank implemented a direct API integration for automated retrieval.
- **Value:** The bank receives disclosure-based ESG indicators with transparent lineage, pays only for needed datasets, and integrates data directly into internal systems through API access.
- **Quote:** "The combination of high-quality disclosure-based data, a transparent pricing model, and API integration makes Dataland a very efficient ESG data source." — **[Name TBD]**, [Role TBD]

#### Story 3: ÖV Braunschweig (Anchor: `#ovbraunschweig`)

- **Tag:** Insurance
- **Title:** Using Dataland as an Independent Source to Validate PAI Data for Audit
- **Summary:** ÖVB uses Dataland as a secondary ESG data source alongside its primary provider to strengthen the robustness of PAI indicator data for SFDR reporting and audit documentation.
- **Challenge:** For SFDR reporting, insurers must demonstrate that ESG indicators used in PAI calculations are reliable and properly documented. Without clear reference to source documents, validation can be difficult during audit reviews.
- **Solution:** ÖVB uses Dataland as a complementary data source specifically for validation. Because Dataland extracts from public disclosures with transparent source references, the team can verify values used in their reporting.
- **Value:** The reporting team can demonstrate to internal stakeholders and external auditors that PAI values are consistent with underlying issuer disclosures.
- **Quote:** "Dataland provides us with a reliable way to verify that the PAI indicators used in our reporting match the issuer's disclosures." — **[Name TBD]**, [Role TBD]

#### Card Design

```scss
.customer-story-detail {
  background: var(--p-surface-50, #f7f7f5);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 16px;
  padding: 32px;
  display: grid;
  grid-template-columns: 24% 76%;
  gap: 24px;

  &__logo {
    width: 100%;
    height: 120px;
    object-fit: contain;
  }

  &__tag {
    font-size: 0.875rem;
    background: var(--p-primary-50);
    color: var(--p-highlight-color);
    padding: 4px 12px;
    border-radius: 999px;
    display: inline-block;
  }

  &__title {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 8px 0 16px;
  }

  &__section-title {
    font-size: 1rem;
    font-weight: 700;
    margin: 16px 0 8px;
  }

  &__text {
    font-size: 1rem;
    color: var(--p-text-muted-color);
    line-height: 1.6;
  }

  &__quote-box {
    background: var(--p-surface-0, #ffffff);
    border: 1px solid var(--p-surface-200);
    border-radius: 12px;
    padding: 20px;
    margin-top: 16px;

    &-text {
      font-style: italic;
      line-height: 1.5;
    }
    &-author {
      font-size: 0.875rem;
      font-weight: 700;
      margin-top: 12px;
    }
    &-role {
      font-size: 0.875rem;
      color: var(--p-text-muted-color);
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .customer-story-detail {
    grid-template-columns: 1fr;
  }
}
```

---

### 4.10 Section: ProductMembershipPricing

**File:** `src/components/resources/productPage/ProductMembershipPricing.vue` (CREATE)

- **Headline:** `Membership and pricing`
- **Subheadline:** `Full data access. Shared cost for new data procurement.`
- **Anchor:** `#membership-pricing`

**3 value proposition blocks:**

| # | Icon | Title | Text |
|---|------|-------|------|
| 1 | `icon_data_access` | Full data access | All datasets available on Dataland can be accessed and used for internal purposes without restrictions |
| 2 | `icon_requesting` | On-demand data sourcing | Missing datasets can be requested and are delivered automatically through Active Portfolio Monitoring |
| 3 | `icon_community` | Shared cost model | The cost of sourcing datasets is shared among members requesting the same data |

**Pricing card (left column):**

```
Title: "Membership and credits"
- €5,000 per year membership
- Includes 100 credits
- 1 credit corresponds to one dataset
- Additional 100 credits for €5,000
Footer: "Credits are only used when new datasets are sourced"
```

**Credits visual (right column):**

- Title: `Cost per dataset decreases as more members request the same data`
- Image: Credits visual diagram (placeholder: `img_credits.svg`)
- Caption: `Credits are split automatically between members requesting the same dataset`

**Bottom note:** `Members only pay for data that is not yet available. All existing datasets are immediately accessible without additional cost.`

#### Layout

- Value props: 3-column grid
- Pricing + Credits: 2-column grid (50/50)
- Mobile: Everything stacks vertically

---

### 4.11 Section: ProductDocumentation

**File:** `src/components/resources/productPage/ProductDocumentation.vue` (CREATE)

- **Headline:** `Documentation and tutorials`
- **Anchor:** `#documentation`

**Documentation links:**

| Label | URL |
|-------|-----|
| Framework documentation overview | `https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation` |
| Backend API documentation | `https://dataland.com/api/swagger-ui/index.html` |
| Document manager API | `https://dataland.com/documents/swagger-ui/index.html` |
| Community manager | `https://dataland.com/community/swagger-ui/index.html` |
| Quality assurance service | `https://dataland.com/qa/swagger-ui/index.html` |
| Users API | `https://dataland.com/users/swagger-ui/index.html` |
| Data sourcing API | `https://dataland.com/data-sourcing/swagger-ui/index.html` |
| Accounting API | `https://dataland.com/accounting/swagger-ui/index.html` |
| Specifications | `https://dataland.com/specifications/swagger-ui/index.html` |

Each link is styled as a pill-shaped text link with a hover outline effect.

**CTA buttons:**

| Button | Label | Link |
|--------|-------|------|
| 1 | Get in touch | `/about#contact` |
| 2 | Try it free | Keycloak register |

---

## 5. About Page Specification

### 5.1 Section Order

```
1. TheAboutCompany         (Company intro with logos)
2. TheAboutTeam            (Leadership team)
3. TheAboutPartners        (Integration partners)
4. TheAboutUpdates         (News grid)
5. TheAboutContact         (Contact info + form layout)
```

### 5.2 Route

Existing route `/about` — no change needed.

### 5.3 AboutPage.vue Template

**File:** `src/components/pages/AboutPage.vue` (MODIFY)

```html
<template>
  <main id="main-content" role="main">
    <TheAboutCompany />
    <TheAboutTeam />
    <TheAboutPartners />
    <TheAboutUpdates />
    <TheAboutContact />
    <ContactInquiryModal />
  </main>
</template>
```

---

### 5.4 Section: TheAboutCompany

**File:** `src/components/resources/aboutPage/TheAboutCompany.vue` (CREATE or MODIFY from TheAboutHero)

- **Anchor:** `#company`

**Two-column layout (40% left, 60% right):**

**Left column (logos):**

- Dataland logo (large, 56px height)
- Werte-Stiftung logo (48px height)
- d-fine logo + PwC logo (side by side, 42px height each)

**Right column (text):**

- **Title:** `Company`
- **Text 1:** `Dataland is a non-profit initiative building shared infrastructure for high-quality sustainability data. The platform provides structured, source-based ESG data to financial institutions across Europe.`
- **Text 2:** `Dataland is part of the Werte-Stiftung foundation, with strategic support from d-fine and PwC.`

**Responsive:** Stacks vertically on mobile (logos above text).

---

### 5.5 Section: TheAboutTeam

**File:** `src/components/resources/aboutPage/TheAboutTeam.vue` (MODIFY)

- **Headline:** `Leadership team`
- **Anchor:** `#team`
- **Background:** Dark blue (`#0f3a82`) — scoped to this section only

**3 team member cards:**

| # | Photo | Name | Role | Email | LinkedIn |
|---|-------|------|------|-------|----------|
| 1 | `img_team_moritz` | Moritz Kiese | Managing Director | `moritz.kiese@dataland.com` | LinkedIn profile |
| 2 | `img_team_andreas` | Andreas Pusch | Product Owner | `andreas.hoecherl@dataland.com` | LinkedIn profile |
| 3 | `img_team_soeren` | Soeren Vorsmann | Operations & Customer Relations | `soeren.vorsmann@dataland.com` | LinkedIn company page |

**Card design:** Dark blue background, white text, portrait photo (cover fit, 320px height), name + role + email/LinkedIn icon links.

**Layout:** 3-column grid on desktop, 2-column on tablet, 1-column on mobile.

---

### 5.6 Section: TheAboutPartners

**File:** `src/components/resources/aboutPage/TheAboutPartners.vue` (MODIFY)

- **Headline:** `Integration partners`
- **Anchor:** `#partners`

**2 partner logos (side by side):**

| Partner | Logo | Link |
|---------|------|------|
| FACT First Cloud | `logo_fact` | `https://www.fact.de/unsere-loesungen/first-cloud/` |
| ISS (Sopra Steria) | `logo_iss` | `https://iss.soprasteria.de/` |

Each logo is clickable and links to the partner's website. Logo height: 120px, contain fit.

**Layout:** 2-column grid (45% each, 10% gap), centered. On mobile: stacks vertically.

---

### 5.7 Section: TheAboutUpdates

**File:** `src/components/resources/aboutPage/TheAboutUpdates.vue` (CREATE)

- **Headline:** `News and updates`
- **Anchor:** `#updates`

**3x3 grid of the same 9 news cards** used in the landing page `TheNewsInsights` section. Same card design, same content, but displayed as a static grid (not a slider).

**Bottom text:** `Follow our latest developments, events and product updates`

**CTA buttons:** Same as landing page news section (Follow on LinkedIn, Subscribe to Newsletter).

**Layout:** 3-column grid on desktop, 2 columns on tablet, 1 column on mobile.

---

### 5.8 Section: TheAboutContact

**File:** `src/components/resources/aboutPage/TheAboutContact.vue` (CREATE)

- **Anchor:** `#contact`
- **Background:** Dark (`#111111`)

**Two-column layout (45% left, 55% right):**

**Left column (contact info, white text):**

- **Title:** `Dataland` (40px, bold)
- **Company:** `Dataland GmbH`
- **Address:** `Am Steinernen Stock 1, 60320 Frankfurt am Main, Deutschland`
- **Phone:** `+49 1622 63 13 04` (linked as `tel:`)
- **Email:** `info@dataland.com` (linked as `mailto:`)

**Right column (form layout):**

Static layout of a "Request a demo" form card. The form includes fields for Name, Email, and Message, plus a privacy consent checkbox and submit button. **Form submission logic is deferred to the backlog** — the form renders visually but does not submit data.

**Form card design:**

```scss
.contact-form {
  background: rgba(0, 40, 55, 0.45);
  border: 1px solid #2f6fe4;
  border-radius: 24px;
  padding: 40px;

  &__title {
    font-size: 2rem;
    color: #ffffff;
    font-weight: 700;
  }

  &__subtitle {
    font-size: 1.25rem;
    color: var(--p-surface-200);
  }

  &__input {
    background: transparent;
    border: 1px solid #4a4a4a;
    border-radius: 8px;
    color: #9a9a9a;
  }

  &__submit {
    background: var(--p-primary-color);
    color: #ffffff;
  }
}
```

**Responsive:** Stacks vertically on mobile (contact info above form).

---

## 6. Additional Pages

### 6.1 Newsletter Page

**Route:** `/newsletter`
**File:** `src/components/pages/NewsletterPage.vue` (CREATE)

**Layout:** Same dark background and two-column structure as TheAboutContact.

**Left column:**

- **Title:** (empty in Valeria's spec — use `Stay informed`)
- **Text 1:** `Receive updates on new datasets, framework coverage, events, and platform developments.`

**Right column:**

Static layout of a newsletter signup form card with fields for Name, Email, Organisation, privacy consent, and "Subscribe" button. **Form submission logic is deferred to the backlog.**

### 6.2 Contact Page

**Route:** `/contact`
**File:** `src/components/pages/ContactPage.vue` (CREATE)

Reuses the `TheAboutContact` section component. Same layout and content as `/about#contact` but as a standalone page.

### 6.3 Testimonials Page

**Route:** `/testimonials`
**File:** `src/components/pages/TestimonialsPage.vue` (CREATE)

- **Headline:** `Video testimonials`
- **Subheadline:** `What our members and partners say about Dataland`

**3x4 grid of video testimonial cards** (12 total, same testimonial data as the landing page carousel). Each card shows a video player area (placeholder — actual video embed is deferred to backlog as it requires YouTube API / cookie consent integration), plus author name and affiliation below.

**Layout:** 3-column grid on desktop, 2 on tablet, 1 on mobile.

**Note:** The video embed functionality (YouTube IFrame API, cookie consent) is **deferred to the backlog**. The initial implementation shows the testimonial cards with text quotes only (same as landing page), with a visual placeholder where the video would appear.

---

## 7. Content Data

### 7.1 Shared Content Files

Content is stored in TypeScript files following the existing `aboutContent.ts` pattern:

| File | Contents |
|------|----------|
| `src/components/resources/landingPage/landingContent.ts` | Testimonials array, news items array, customer story summaries, sector tiles |
| `src/components/resources/productPage/productContent.ts` | Use cases, feature cards, pricing data, documentation links, detailed customer stories |
| `src/components/resources/aboutPage/aboutContent.ts` | Team members, partner logos, company text (MODIFY existing) |

### 7.2 URL Constants

All external URLs used across the site:

| Constant | URL |
|----------|-----|
| `URL_LINKEDIN_DATALAND` | `https://www.linkedin.com/company/dataland-gmbh` |
| `URL_DFINE` | `https://www.d-fine.com` |
| `URL_PWC` | `https://www.pwc.com` |
| `URL_PARTNER_FACT` | `https://www.fact.de/unsere-loesungen/first-cloud/` |
| `URL_PARTNER_ISS` | `https://iss.soprasteria.de/` |
| `CONTACT_MORITZ_MAIL` | `mailto:moritz.kiese@dataland.com` |
| `CONTACT_MORITZ_LINKEDIN` | `https://www.linkedin.com/in/moritz-kiese-932b104/` |
| `CONTACT_ANDREAS_MAIL` | `mailto:andreas.hoecherl@dataland.com` |
| `CONTACT_ANDREAS_LINKEDIN` | `https://www.linkedin.com/in/andreas-h%C3%B6cherl-016220b4/` |
| `CONTACT_SOEREN_MAIL` | `mailto:soeren.vorsmann@dataland.com` |
| `CONTACT_SOEREN_LINKEDIN` | `https://www.linkedin.com/company/dataland-gmbh` |
| `CONTACT_PHONE` | `tel:+491622631304` |
| `CONTACT_EMAIL` | `mailto:info@dataland.com` |

News post URLs (LinkedIn):

| Constant | URL |
|----------|-----|
| `URL_NEWS_EU_TAXO` | `https://www.linkedin.com/feed/update/urn:li:activity:7435335342439735296` |
| `URL_NEWS_BVI_FOK` | `https://www.linkedin.com/feed/update/urn:li:activity:7432564767090905089` |
| `URL_NEWS_DMM_Q12026` | `https://www.linkedin.com/feed/update/urn:li:activity:7430511455638118400` |
| `URL_NEWS_2025` | `https://www.linkedin.com/feed/update/urn:li:activity:7419695872156028928` |
| `URL_NEWS_SFDR2` | `https://www.linkedin.com/feed/update/urn:li:activity:7404533321671589890` |
| `URL_NEWS_PCAF` | `https://www.linkedin.com/feed/update/urn:li:activity:7397576850782441472` |
| `URL_NEWS_SUST2025` | `https://www.linkedin.com/feed/update/urn:li:activity:7395135444641947648` |
| `URL_NEWS_DMMQ42025` | `https://www.linkedin.com/feed/update/urn:li:activity:7392146170766151680` |
| `URL_NEWS_ERIK` | `https://www.linkedin.com/feed/update/urn:li:activity:7391407400764772352` |

API documentation URLs:

| Constant | URL |
|----------|-----|
| `URL_DOC_FRAMEWORK` | `https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation` |
| `URL_DOC_BACKEND_API` | `https://dataland.com/api/swagger-ui/index.html` |
| `URL_DOC_DOCUMENT_MANAGER` | `https://dataland.com/documents/swagger-ui/index.html` |
| `URL_DOC_COMMUNITY_MANAGER` | `https://dataland.com/community/swagger-ui/index.html` |
| `URL_DOC_QA` | `https://dataland.com/qa/swagger-ui/index.html` |
| `URL_DOC_USERS` | `https://dataland.com/users/swagger-ui/index.html` |
| `URL_DOC_DATA_SOURCING` | `https://dataland.com/data-sourcing/swagger-ui/index.html` |
| `URL_DOC_ACCOUNTING` | `https://dataland.com/accounting/swagger-ui/index.html` |
| `URL_DOC_SPECIFICATIONS` | `https://dataland.com/specifications/swagger-ui/index.html` |

### 7.3 Image Assets Required

**From Valeria's spec (new assets to add):**

| Asset | Path | Notes |
|-------|------|-------|
| Intro illustration | `/static/images/intro_art.svg` | Hero illustration |
| Arrow (problem-solution) | `/static/images/arrow_big.svg` | Used in Why Us and Product page blocks |
| Credits visual | `/static/images/img_credits.svg` | Pricing section diagram |
| News images (9) | `/static/images/news_*.png` | News card thumbnails |
| Sector icons (11) | `/static/images/icon_*.svg` | Customer Profiles section |
| Customer logos (MEAG, NORD/LB, ÖVB) | `/static/logos/logo_meag.svg`, etc. | Customer Stories section |
| Member logos (14+) | `/static/logos/logo_*.svg` | Trusted By carousel |
| Partner logos (FACT, ISS) | `/static/logos/logo_fact.svg`, etc. | About Partners section |
| Team photos (3) | `/static/images/img_team_*.jpg` | About Team section |
| LinkedIn icon | `/static/images/icon_linkedin.svg` | Footer and team cards |

---

## 8. Implementation Checklist

### 8.1 Files to CREATE

| File | Description |
|------|-------------|
| `src/assets/scss/breakpoints.scss` | SCSS breakpoint variables |
| `src/composables/useBreakpoint.ts` | Shared reactive breakpoint composable |
| `src/components/pages/ProductPage.vue` | Product page shell |
| `src/components/pages/NewsletterPage.vue` | Newsletter page (static layout) |
| `src/components/pages/ContactPage.vue` | Contact page (reuses About contact section) |
| `src/components/pages/TestimonialsPage.vue` | Testimonials page |
| `src/components/resources/landingPage/TheFindLei.vue` | Company search section |
| `src/components/resources/landingPage/TheWhyUs.vue` | Problem-solution section |
| `src/components/resources/landingPage/TheTrustedBy.vue` | Logo carousel |
| `src/components/resources/landingPage/TheCustomerStories.vue` | Story cards |
| `src/components/resources/landingPage/TheTestimonials.vue` | Quote carousel |
| `src/components/resources/landingPage/TheFrameworks.vue` | Framework cards |
| `src/components/resources/landingPage/TheCustomerProfiles.vue` | Sector mosaic |
| `src/components/resources/landingPage/TheNewsInsights.vue` | News slider |
| `src/components/resources/landingPage/landingContent.ts` | Landing page content data |
| `src/components/resources/productPage/ProductIntro.vue` | Product intro |
| `src/components/resources/productPage/ProductHowItWorks.vue` | How it works |
| `src/components/resources/productPage/ProductGettingData.vue` | Getting data |
| `src/components/resources/productPage/ProductFeatures.vue` | Features grid |
| `src/components/resources/productPage/ProductUseCases.vue` | Use cases |
| `src/components/resources/productPage/ProductCustomerStories.vue` | Detailed customer stories |
| `src/components/resources/productPage/ProductMembershipPricing.vue` | Pricing section |
| `src/components/resources/productPage/ProductDocumentation.vue` | Documentation links |
| `src/components/resources/productPage/productContent.ts` | Product page content data |
| `src/components/resources/aboutPage/TheAboutCompany.vue` | Company section |
| `src/components/resources/aboutPage/TheAboutUpdates.vue` | News grid |
| `src/components/resources/aboutPage/TheAboutContact.vue` | Contact section |
| `src/components/generics/ProblemSolutionBlock.vue` | Reusable problem-solution arrow component |
| `src/components/generics/AccessibleCarousel.vue` | Reusable carousel with pause, keyboard, ARIA |
| `src/components/generics/NewsCard.vue` | Reusable news card component |

### 8.2 Files to MODIFY

| File | Changes |
|------|---------|
| `src/components/pages/LandingPage.vue` | Complete section rewrite — new sections and order |
| `src/components/pages/AboutPage.vue` | Update section order and imports |
| `src/components/generics/LandingPageHeader.vue` | Add Product/About dropdowns, hamburger menu, skip-link |
| `src/components/generics/LandingPageFooter.vue` | Redesign to 4-column layout |
| `src/components/resources/landingPage/TheIntro.vue` | Two-column hero layout |
| `src/components/resources/aboutPage/TheAboutTeam.vue` | Update team member data |
| `src/components/resources/aboutPage/TheAboutPartners.vue` | Update partner list |
| `src/components/resources/aboutPage/aboutContent.ts` | Update team and partner data |
| `src/router/index.ts` | Add routes for /product, /newsletter, /contact, /testimonials |

### 8.3 Files to DELETE (after new components are in place)

| File | Reason |
|------|--------|
| `src/components/resources/landingPage/TheQuotes.vue` | Replaced by TheTestimonials |
| `src/components/resources/landingPage/TheHowItWorks.vue` | Replaced by Product page sections |
| `src/components/resources/landingPage/TheJoinCampaign.vue` | Replaced by TheFrameworks |
| `src/components/resources/landingPage/TheStruggle.vue` | Replaced by TheWhyUs |
| `src/components/resources/aboutPage/TheAboutSponsors.vue` | Merged into footer / About company |
| `src/components/resources/aboutPage/TheAboutHero.vue` | Replaced by TheAboutCompany |
| `src/components/resources/aboutPage/TheAboutTrustPillars.vue` | Removed from About page |
| `src/components/resources/aboutPage/TheAboutPrinciples.vue` | Removed from About page |
| `src/components/resources/aboutPage/TheAboutEcosystem.vue` | Replaced by TheAboutPartners |
| `src/components/resources/aboutPage/TheAboutBottomCTA.vue` | Replaced by TheAboutContact |

### 8.4 Implementation Order (Recommended)

1. **Create infrastructure:** `breakpoints.scss`, `useBreakpoint.ts`, reduced motion support
2. **Create shared components:** `ProblemSolutionBlock`, `AccessibleCarousel`, `NewsCard`
3. **Create content data files:** `landingContent.ts`, `productContent.ts`, update `aboutContent.ts`
4. **Create Product page:** All 8 sections + `ProductPage.vue` + route
5. **Rework Landing page:** All 10 sections + update `LandingPage.vue`
6. **Rework About page:** All 5 sections + update `AboutPage.vue`
7. **Create additional pages:** Newsletter, Contact, Testimonials + routes
8. **Update navigation:** Header dropdowns, hamburger menu, skip-link
9. **Redesign footer:** 4-column layout
10. **Copy image assets:** All new logos, icons, news images from Valeria's asset directory
11. **Delete old components:** After verification
12. **Verify:** `npm run typecheck`, `npm run lint`, `npm run testcomponent`

---

## Appendix: Visual Flow Diagrams

### Landing Page

```
+--------------------------------------------------+
| [Logo]    [Product ▾] [About ▾]   [Login][Try it] |  <- Header (sticky)
+--------------------------------------------------+
|                                                    |
|  Democratizing access to         [Illustration]   |
|  high-quality sustainability                       |
|  data: A European non-profit                       |  <- TheIntro (Hero)
|  shared data platform                              |
|                                                    |
|  [Try it free]  [Get in touch]                     |
+--------------------------------------------------+
|  Search sustainability data by company name...     |  <- TheFindLei
|  [_________________________________]               |
+--------------------------------------------------+
|  Recurring problems... Dataland addresses them     |
|                                                    |
|  Missing data    →    Data on demand               |  <- TheWhyUs
|  Poor quality    →    AI + human verification      |
|  Restricted use  →    Unrestricted internal use    |
|  High prices     →    Lean pricing model           |
|                                                    |
|  [Features] [Use Cases] [Get in touch]             |
+--------------------------------------------------+
|  Trusted by companies of different sizes...        |
|  [← logo | logo | logo | logo →]  [⏸]            |  <- TheTrustedBy
+--------------------------------------------------+
|  Customer stories                                  |
|  [MEAG card] [NORD/LB card] [ÖVB card]            |  <- TheCustomerStories
+--------------------------------------------------+
|  What our members share...                         |
|  [← quote | quote | quote | quote →]  [⏸]        |  <- TheTestimonials
|  [To the video testimonials]                       |
+--------------------------------------------------+
|  Dataland supports the key ESG frameworks...       |
|  [EU Tax Fin] [EU Tax Non-Fin] [EU Tax Gas]        |  <- TheFrameworks
|  [SFDR]       [PCAF]           [LkSG]              |
|  [News & insights] [Subscribe to Newsletter]       |
+--------------------------------------------------+
|  ...flexibility and breadth across sectors...      |
|  [Banks]    [Insurance]  [Asset Mgrs]              |
|  [Pension]  [Public Fin] [Providers]               |  <- TheCustomerProfiles
|  [FinData] [ESG Soft] [Industry] [Sust] [Acad]    |
+--------------------------------------------------+
|  We are constantly developing...                   |
|  [← news | news | news →]  [⏸]                   |  <- TheNewsInsights
|  [Follow on LinkedIn] [Subscribe Newsletter]       |
+--------------------------------------------------+
| Dataland | Product   | Company     | Resources    |
| desc.    | Features  | Why DL      | Tutorials    |  <- Footer
| logos    | Frameworks| About us    | Platform doc |
|          | Use Cases | Updates     | Tech Hub     |
|          | ...       | Partners    |              |
|          |           | Contact     |              |
| Legal | Imprint | Privacy | Cookies | © | [in]    |
+--------------------------------------------------+
```

### Product Page

```
+--------------------------------------------------+
|  Structured company-level sustainability data...   |  <- ProductIntro
+--------------------------------------------------+
|  How it works                                      |
|  UI access        →  Visual exploration            |  <- ProductHowItWorks
|  API integration  →  Automated workflows           |
|  Partner integr.  →  Embedded data services        |
+--------------------------------------------------+
|  Accessing datasets as a member                    |  <- ProductGettingData
|  Already available → No additional cost            |
|  Not yet available → Delivered within one month    |
+--------------------------------------------------+
|  Platform features                                 |
|  [Download] [Portfolio mgmt] [Portfolio share]     |  <- ProductFeatures
|  [Request]  [Source transp.] [Flexible formats]    |
+--------------------------------------------------+
|  Use Cases                                         |
|  [9 use case blocks]                               |  <- ProductUseCases
+--------------------------------------------------+
|  Customer stories                                  |
|  [MEAG detailed story]                             |  <- ProductCustomerStories
|  [NORD/LB detailed story]                          |
|  [ÖVB detailed story]                              |
+--------------------------------------------------+
|  Membership & Pricing                              |
|  [Full access] [On-demand] [Shared cost]           |  <- ProductMembershipPricing
|  [Pricing card]    [Credits visual]                |
+--------------------------------------------------+
|  Documentation and tutorials                       |
|  [9 API doc links]                                 |  <- ProductDocumentation
|  [Get in touch]  [Try it free]                     |
+--------------------------------------------------+
```

### About Page

```
+--------------------------------------------------+
|  [DL logo]           Company                       |
|  [Wertestiftung]     Dataland is a non-profit...  |  <- TheAboutCompany
|  [d-fine] [PwC]      Dataland belongs to...       |
+--------------------------------------------------+
|  Leadership team                   (dark blue bg) |
|  [Moritz]  [Andreas]  [Soeren]                    |  <- TheAboutTeam
+--------------------------------------------------+
|  Integration partners                              |
|  [FACT First Cloud]  [ISS / Sopra Steria]         |  <- TheAboutPartners
+--------------------------------------------------+
|  News and updates                                  |
|  [news] [news] [news]                              |  <- TheAboutUpdates
|  [news] [news] [news]                              |
|  [news] [news] [news]                              |
|  [Follow LinkedIn] [Subscribe Newsletter]          |
+--------------------------------------------------+
|  Dataland              [Request a demo form]       |
|  Dataland GmbH         [Name field]               |  <- TheAboutContact
|  Address               [Email field]  (dark bg)   |
|  Phone / Email         [Message field]            |
|                        [Submit]                    |
+--------------------------------------------------+
```
