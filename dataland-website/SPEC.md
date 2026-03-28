# Dataland Website — Migration Specification

## Overview

Migrate the public-facing marketing pages from `dataland-frontend` (Vue SPA) into a standalone `dataland-website` using Astro + Tailwind CSS. This is a **content-preserving migration**, not a redesign — all copy, layout intent, and content data come from the existing Vue implementation.

**Goals:**
- Static HTML output (no blank page without JS)
- Mobile-first responsive design
- ~80% reduction in lines of code (inlined sections, Tailwind utilities, no single-use components)
- Zero coupling to PrimeVue or the platform SPA

**Non-goals:**
- Changing copy, adding sections, or redesigning layouts — match the existing Vue pages
- Introducing a CMS, build-time data fetching, or dynamic server rendering

---

## Content Source of Truth

All copy text, content arrays, and URLs are defined in the existing Vue codebase. **Do not duplicate content into this spec.** Instead, extract from these files at build time or copy them into Astro data files:

| Source file (in `dataland-frontend/src/components/resources/`) | Contains |
|---|---|
| `landingPage/landingContent.ts` | TESTIMONIALS (12), NEWS_ITEMS (9), CUSTOMER_STORY_SUMMARIES (3), SECTOR_TILES (11), WHY_US_PAIRS (4), FRAMEWORK_CARDS (6), TRUSTED_BY_LOGOS (14) |
| `productPage/productContent.ts` | USE_CASES (7), FEATURE_CARDS (6), HOW_IT_WORKS_BLOCKS (3), GETTING_DATA_BLOCKS (2), VALUE_PROPOSITIONS (3), PRICING_CARD, CREDITS_VISUAL, PRICING_BOTTOM_NOTE, CUSTOMER_STORIES_DETAILED (3), DOCUMENTATION_LINKS (9), all URL constants, contact constants |
| `aboutPage/aboutContent.ts` | LEADERSHIP_TEAM (3), PARTNERS (2), COMPANY_COPY |
| `successStories/successStoryContent.ts` | SUCCESS_STORIES (3) with full challenge/process/result text |

When creating Astro data files (`src/data/*.ts`), copy the arrays and types verbatim from these sources. Adapt only the import paths and remove any Vue-specific type dependencies.

---

## Brand & Tokens

| Token | Value |
|---|---|
| Primary (orange) | `#ff6813` |
| Secondary (dark teal) | `#013d48` |
| Surface background | `#f7f7f5` |
| Surface border | `#e6e6e6` |
| Font | IBM Plex Sans (400, 600, 700) |
| Tagline | "Non-profit sustainability data" |
| Cookiebot ID | `cba5002e-6f0e-4848-aadc-ccc8d5c96c86` |

Define as CSS custom properties in `src/styles/global.css`:

```css
@import 'tailwindcss';

:root {
  --color-primary: #ff6813;
  --color-secondary: #013d48;
  --color-surface-0: #ffffff;
  --color-surface-50: #f7f7f5;
  --color-surface-200: #e6e6e6;
  --color-text: #1a1a1a;
  --color-text-muted: #6b7280;
  --font-family: 'IBM Plex Sans', sans-serif;
}
```

---

## Breakpoints

Use Tailwind's default breakpoints, which closely match the existing spec:

| Tailwind | Pixels | Old spec equivalent | Notes |
|---|---|---|---|
| `sm` | 640px | `$bp-sm` | Mobile |
| `md` | 768px | `$bp-md` | iPad portrait — common in German financial services |
| `lg` | 1024px | `$bp-lg` | Tablet landscape / small desktop |
| `xl` | 1440px | `$bp-xl` | Prevents excessive stretching on wide monitors |

All layouts are **mobile-first** (base styles = mobile, `md:` = tablet, `lg:` = desktop, `xl:` = wide).

---

## Typography Scale

| Element | Size (mobile → desktop) | Weight |
|---|---|---|
| Hero headline | 32px → 48px (`text-3xl lg:text-5xl`) | 700 |
| Section headline | 24px → 32px (`text-2xl lg:text-3xl`) | 700 |
| Card title | 20px (`text-xl`) | 700 |
| Body text | 16px (`text-base`) | 400 |
| Small / meta | 14px (`text-sm`) | 400 |
| Caption | 12px (`text-xs`) | 400 |

---

## Pages

### Landing Page (`/`)

Sections inlined in `index.astro`, in this order:

#### 1. Hero
- Two-column: 60% text / 40% platform screenshot
- Headline with orange highlight on second line
- Subtext: one line of differentiators
- Primary CTA: "Try it free" → Keycloak register
- Secondary CTA: "Get in touch" → `/about#contact`
- Screenshot: `platform-screenshot.png` with CSS 3D float (`perspective(1500px) rotateY(-5deg) rotateX(2deg)`, layered box-shadow). On mobile: reduce rotation, keep subtle shadow, stack vertically
- **Copy source:** Inline in existing `TheIntro.vue`

#### 2. LEI Search
- Heading + company search bar (Vue island: `CompanySearch.vue`)
- Calls public Dataland API, autocomplete after 4 chars, navigates to `/companies/{id}`
- Centered, max-width 520px
- **Copy source:** Inline in existing `TheFindLei.vue`

#### 3. Why Us
- Section headline
- 4 problem → solution pairs, each as a 3-column row: problem (40%) | arrow (20%) | solution (40%)
- Arrow SVG centered vertically, `aria-hidden="true"`
- Mobile: stack vertically, arrow hidden, separated by divider
- 3 CTA buttons below: Features | Use Cases | Get in touch
- **Content source:** `WHY_US_PAIRS` from `landingContent.ts`

#### 4. Trusted By
- Logo grid of 14 members
- Logos normalized to same `max-height` (let width vary naturally)
- Static grid (not carousel) — simpler than the old spec's auto-scrolling carousel, fewer JS dependencies
- **Content source:** `TRUSTED_BY_LOGOS` from `landingContent.ts`

#### 5. Customer Stories
- 3 story summary cards: logo, orange pill tag, description, link to `/product#anchor`
- 3-column grid → 1 column on mobile
- **Content source:** `CUSTOMER_STORY_SUMMARIES` from `landingContent.ts`

#### 6. Testimonials
- Horizontal scrollable row of quote cards (CSS `overflow-x: auto`, `scroll-snap-type: x mandatory`)
- No JS carousel — pure CSS scroll with snap points
- CTA: "Watch member testimonials" → `/testimonials`
- **Content source:** `TESTIMONIALS` from `landingContent.ts`

#### 7. Frameworks
- 6 cards in 3×2 grid (2×3 tablet, 1-col mobile)
- Each card: title, optional subtitle, description, subtle orange corner accent (CSS `clip-path` triangle)
- Bottom text + CTA
- **Content source:** `FRAMEWORK_CARDS` from `landingContent.ts`

#### 8. Customer Profiles
- Sector mosaic: 11 tiles with icon + label
- CSS Grid weighted layout: 3 XL tiles (row 1), 3 L tiles (row 2), 5 S tiles (row 3)
- Tablet: uniform 3-col, Mobile: 2-col
- **Content source:** `SECTOR_TILES` from `landingContent.ts`

#### 9. News & Insights
- 9 news cards with image, title, date, LinkedIn link (opens in new tab)
- Images near-square aspect ratio
- 3-column grid, scrollable on mobile
- **Content source:** `NEWS_ITEMS` from `landingContent.ts`

---

### Product Page (`/product`)

Sections inlined in `product.astro`:

#### 1. Intro
- Centered headline (short declarative fragments)
- **Copy source:** Inline in `ProductIntro.vue`

#### 2. How It Works (`#how-it-works`)
- 3 problem→solution rows (same visual pattern as Why Us)
- **Content source:** `HOW_IT_WORKS_BLOCKS` from `productContent.ts`

#### 3. Getting Data (`#getting-data`)
- 2 problem→solution rows
- **Content source:** `GETTING_DATA_BLOCKS` from `productContent.ts`

#### 4. Features (`#features`)
- 6 cards in 3×2 grid, same card style as frameworks (orange corner accent)
- **Content source:** `FEATURE_CARDS` from `productContent.ts`

#### 5. Use Cases (`#use-cases`)
- 7 use case blocks with title + description text
- Alternating layout (not problem→solution arrows — just title + text blocks)
- **Content source:** `USE_CASES` from `productContent.ts`

#### 6. Customer Stories (`#customer-stories`, `#meag`, `#nordlb`, `#ovbraunschweig`)
- 3 detailed story cards: logo (24% left) + content (76% right)
- Each has: tag, title, summary, challenge, solution, value, quote box
- Mobile: single column
- **Content source:** `CUSTOMER_STORIES_DETAILED` from `productContent.ts`

#### 7. Membership & Pricing (`#membership-pricing`)
- 3 value proposition icons
- 2-column: pricing card (left) + credits visual (right)
- Mobile: stacks
- **Content source:** `VALUE_PROPOSITIONS`, `PRICING_CARD`, `CREDITS_VISUAL`, `PRICING_BOTTOM_NOTE` from `productContent.ts`

#### 8. Documentation (`#documentation`)
- 9 API doc links as pill-shaped text links
- 2 CTAs: Get in touch | Try it free
- **Content source:** `DOCUMENTATION_LINKS` from `productContent.ts`

---

### About Page (`/about`)

Sections inlined in `about.astro`:

#### 1. Company (`#company`)
- Two-column: logos left (40%), company text right (60%)
- Logos: Dataland, Werte-Stiftung, d-fine + PwC
- Mobile: stacks (logos above text)
- **Content source:** `COMPANY_COPY` from `aboutContent.ts`

#### 2. Team (`#team`)
- Dark blue background (`#0f3a82`), white text
- 3 team cards: photo, name, role, email icon, LinkedIn icon
- 3-col → 2-col → 1-col responsive
- **Content source:** `LEADERSHIP_TEAM` from `aboutContent.ts`
- **Note:** Andreas Höcherl replaces Andreas Pusch (updated name, same email)

#### 3. Partners (`#partners`)
- 2 partner cards with logo + link
- Now 4 partners: FACT First Cloud, ISS/Sopra Steria, Eskua AI, Keynum
- Logo height: 120px, contain fit
- Keynum logo is white/blue on transparent — render on a dark card or use CSS filter
- **Content source:** `PARTNERS` from `aboutContent.ts` (extend with 2 new entries)

#### 4. Updates (`#updates`)
- Static 3×3 grid of same 9 news cards (not a slider)
- Same card design as landing page News section
- **Content source:** `NEWS_ITEMS` from `landingContent.ts`

#### 5. Contact (`#contact`)
- Dark background (`#111111`), white text
- Two-column: contact info left, demo request form right
- Form is **visual only** (no submission logic)
- **Content source:** Contact constants from `productContent.ts`

---

### Testimonials Page (`/testimonials`)

Built in `testimonials.astro`:

- Headline + subheadline
- 3×4 responsive grid of 12 video cards
- Each card: YouTube embed (Cookiebot-gated), name, affiliation
- Consent flow: `cookieconsent-optin-marketing` / `cookieconsent-optout-marketing` CSS classes
- Responsive: 3-col → 2-col → 1-col
- **Content source:** `TESTIMONIALS` from `landingContent.ts` + YouTube IDs (see table in current SPEC)

---

### Success Stories (`/success-stories/:slug`)

MDX-driven static pages via Astro content collections.

- 3 stories: MEAG, NORD/LB, ÖVB (MDX files already created in `src/content/success-stories/`)
- Template: title, metadata, challenge/process/result sections, quote, CTA
- **Content source:** MDX frontmatter + body, supplemented by `SUCCESS_STORIES` from `successStoryContent.ts`

---

### Pages NOT migrated (stay in SPA)

`/dataprivacy`, `/terms`, `/pricing`, `/imprint` — footer links point to the SPA for these.

### Pages removed

- `/token` — deleted
- `/newsletter` — skipped (was static-only placeholder)
- `/contact` — merged into `/about#contact`

---

## Navigation

### Header (`Header.astro` + `MobileNav.vue`)

**Desktop (≥ lg):**
```
[Logo]          [Product]  [About]          [Login] [Try it free]
```

- Product and About are plain links (no dropdowns — simplification from old spec's click-triggered dropdowns, reduces JS and complexity)
- Login → Keycloak login
- Try it free → Keycloak register (orange button)

**Mobile (< lg):**
- Logo + hamburger → `MobileNav.vue` island (`client:media="(max-width: 1023px)"`)
- Slide-down overlay: all nav links + CTAs
- Close on link click or close icon

**Style:**
- Height: 72px
- Background: `var(--color-surface-50)`
- Border bottom: 1px `var(--color-surface-200)`
- Sticky top, z-50

### Footer (`Footer.astro`)

**4-column layout (desktop), 2×2 (tablet), 1-col (mobile):**

| Dataland | Product | Company | Resources |
|---|---|---|---|
| Tagline + logos (Wertestiftung, d-fine, PwC) | How it works | Why Dataland | Tutorials |
| | Features | About us | Platform documentation |
| | Frameworks | Updates and Insights | Technical Hub |
| | Use Cases | Partners | |
| | Customer Stories | Contact | |
| | Testimonials | | |
| | Membership & Pricing | | |

**Bottom bar:**
```
[Legal] [Imprint] [Data Privacy] [Cookie Settings]    © {year} Dataland    [LinkedIn]
```

Legal links point to SPA routes. Cookie Settings triggers Cookiebot. LinkedIn opens in new tab.

**Footer link targets:** See `productContent.ts` URL constants for all external links.

---

## Simplifications vs. Old Spec

These deliberate simplifications reduce complexity without losing content:

| Old spec (Vue) | Astro migration | Why |
|---|---|---|
| Auto-scrolling carousels with pause, ARIA live regions, keyboard nav | CSS scroll-snap (testimonials) or static grid (trusted-by, news) | Eliminates ~200 LoC of carousel JS + accessibility machinery. CSS scroll-snap is natively accessible |
| Click-triggered nav dropdowns with ARIA menu roles | Plain links (no dropdowns) | Nav has only 2 items (Product, About) — dropdowns add complexity for minimal benefit |
| `ProblemSolutionBlock.vue` shared component | Inline HTML pattern (copy-paste the ~15 lines per instance) | Used in only 3 places. A shared Astro component is fine too, but not required |
| `AccessibleCarousel.vue` generic component | Not needed (no JS carousels) | See above |
| `NewsCard.vue` shared component | Inline card markup or a simple Astro component | Trivial markup, used in 2 places |
| Scoped SCSS with BEM classes | Tailwind utility classes | Eliminates all `.scss` files |
| `useBreakpoint.ts` composable | Tailwind responsive prefixes (`md:`, `lg:`) | No JS needed for responsive layout |
| `breakpoints.scss` | Tailwind defaults | Already aligned |
| `prefers-reduced-motion` global SCSS | Tailwind `motion-reduce:` variant | Built-in |
| Newsletter page | Removed | Was static placeholder with no backend |
| Contact page | Merged into `/about#contact` | Was duplicate of about contact section |

---

## Technical Requirements

### Performance
- Lighthouse mobile ≥ 90
- JS only for: CompanySearch (Vue island), MobileNav (Vue island), YouTube embeds
- Total shipped JS target: ~5–10 kB

### Accessibility
- Semantic HTML (`<section>`, `<main>`, `<nav>`, `<header>`, `<footer>`)
- Skip-to-content link in header
- `id="main-content"` on `<main>`
- `aria-label` on sections (use section headline text)
- Keyboard-navigable interactive elements
- Color contrast: WCAG AA (4.5:1 normal, 3:1 large)
- `motion-reduce:` variant on any animations (hero float, hover effects)
- Touch targets ≥ 44px
- Body text ≥ 16px

### SEO
- Server-rendered HTML (Astro default)
- `<title>`, `<meta name="description">`, OG tags per page
- Semantic heading hierarchy (one `<h1>` per page)
- Auto-generated `sitemap.xml` (Astro sitemap integration)
- `robots.txt`

### Cookie Consent
- Cookiebot script in `<head>`
- YouTube embeds gated on marketing consent
- Use CSS classes: `cookieconsent-optin-marketing` / `cookieconsent-optout-marketing`

---

## Assets

Copy from `dataland-frontend/public/static/`:
- `logos/` — 14 member logos, customer logos (MEAG, NORD/LB, ÖVB), footer logos
- `icons/` — sector icons (11), UI icons
- `images/` — news images (9), `intro_art.svg`, `arrow_big.svg`, `img_credits.svg`, process sketches
- `about/` — team photos (3), partner logos (4, already copied)
- Fonts: IBM Plex Sans
- Favicons: apple-touch-icon, favicon PNGs, safari-pinned-tab, site.webmanifest

---

## Visual Layout Reference

### Landing Page
```
┌──────────────────────────────────────────────────┐
│ [Logo]    [Product]  [About]      [Login][Try it]│  Header (sticky)
├──────────────────────────────────────────────────┤
│                                                  │
│  Headline + subtext          [Platform screenshot│  Hero
│  [Try it free] [Get in touch]  with 3D float]   │
│                                                  │
├──────────────────────────────────────────────────┤
│  Search sustainability data by company name...   │  LEI Search
│  [_________________________________]             │
├──────────────────────────────────────────────────┤
│  Missing data    →    Data on demand             │
│  Poor quality    →    AI + human verification    │  Why Us
│  Restricted use  →    Unrestricted use           │
│  High prices     →    Lean pricing model         │
│  [Features] [Use Cases] [Get in touch]           │
├──────────────────────────────────────────────────┤
│  [logo] [logo] [logo] [logo] [logo] [logo] ...  │  Trusted By
├──────────────────────────────────────────────────┤
│  [MEAG card]  [NORD/LB card]  [ÖVB card]        │  Customer Stories
├──────────────────────────────────────────────────┤
│  [← quote | quote | quote | quote →]             │  Testimonials (CSS scroll-snap)
│  [Watch member testimonials]                     │
├──────────────────────────────────────────────────┤
│  [EU Tax Fin] [EU Tax Non-Fin] [EU Tax Gas]      │
│  [SFDR]       [PCAF]           [LkSG]            │  Frameworks
├──────────────────────────────────────────────────┤
│  [Banks]    [Insurance]  [Asset Mgrs]            │
│  [Pension]  [Public Fin] [Providers]             │  Customer Profiles
│  [FinData] [ESG Soft] [Industry] [Sust] [Acad]  │
├──────────────────────────────────────────────────┤
│  [news] [news] [news]                            │
│  [news] [news] [news]                            │  News & Insights
│  [news] [news] [news]                            │
├──────────────────────────────────────────────────┤
│ Dataland │ Product  │ Company    │ Resources     │  Footer
│ desc.    │ Features │ About us   │ Tutorials     │
│ logos    │ ...      │ ...        │ ...           │
│ Legal │ Imprint │ Privacy │ Cookies │ © │ [in]  │
└──────────────────────────────────────────────────┘
```

### Product Page
```
┌──────────────────────────────────────────────────┐
│  Structured sustainability data...               │  Intro
├──────────────────────────────────────────────────┤
│  Platform access  →  Browse, search, download    │
│  API integration  →  Automated workflows         │  How It Works
│  Partner integr.  →  Embedded data services      │
├──────────────────────────────────────────────────┤
│  Already available → No additional cost          │  Getting Data
│  Not yet available → Delivered within one month  │
├──────────────────────────────────────────────────┤
│  [Download] [Portfolio mgmt] [Portfolio share]   │  Features
│  [Request]  [Source transp.] [Multi-framework]   │
├──────────────────────────────────────────────────┤
│  [7 use case blocks]                             │  Use Cases
├──────────────────────────────────────────────────┤
│  [MEAG detailed story]                           │
│  [NORD/LB detailed story]                        │  Customer Stories
│  [ÖVB detailed story]                            │
├──────────────────────────────────────────────────┤
│  [Full access] [On-demand] [Shared cost]         │  Membership & Pricing
│  [Pricing card]    [Credits visual]              │
├──────────────────────────────────────────────────┤
│  [9 API doc links]                               │  Documentation
│  [Get in touch]  [Try it free]                   │
└──────────────────────────────────────────────────┘
```

### About Page
```
┌──────────────────────────────────────────────────┐
│  [Logos]           Company description            │  Company
├──────────────────────────────────────────────────┤
│  [Moritz]  [Andreas]  [Soeren]    (dark blue bg) │  Team
├──────────────────────────────────────────────────┤
│  [FACT] [ISS] [Eskua] [Keynum]                   │  Partners
├──────────────────────────────────────────────────┤
│  [news] [news] [news]                            │
│  [news] [news] [news]                            │  Updates
│  [news] [news] [news]                            │
├──────────────────────────────────────────────────┤
│  Dataland GmbH        [Request a demo form]      │  Contact (dark bg)
│  Address / Phone      [Name] [Email] [Message]   │
└──────────────────────────────────────────────────┘
```
