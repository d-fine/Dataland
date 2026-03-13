# Landing Page & About Page Rework -- Final Implementation Specification

**Date:** 2026-03-08
**Status:** FINAL -- Ready for implementation
**Branch:** `feature/rework-about-page`

---

## Placeholder Content Inventory

The following items use **placeholder content** for initial implementation. All are marked with `[PLACEHOLDER]` in the spec and in code comments. Replace with real content before production.

| Item | Placeholder Type | Files |
|------|-----------------|-------|
| Platform demo video | No mp4 exists; SVG poster mockup used as fallback | `static/videos/platform-demo-thumb.svg` |
| Team photos (3 people) | SVG avatars with initials (AH, SV, RS, SH) | `static/about/team-*.svg` |
| Advisory board photos (2 people) | SVG avatars with initials | `static/about/team-rudi-siebel.svg`, `team-stephan-henkel.svg` |
| Partner logos (4 companies) | SVG text placeholders | `static/about/logo-*.svg` |
| Member process sketch | Generic SVG flow diagram | `static/images/member-process-sketch.svg` |
| Customer success stories (3) | Fictional names, companies, quotes, figures | `successStoryContent.ts` |
| Per-story process sketches (3) | Generic SVG flow diagrams | `static/images/process-sketch-*.svg` |

---

## Table of Contents

1. [Design Decisions Log](#1-design-decisions-log)
2. [Global Changes](#2-global-changes)
3. [Landing Page Specification](#3-landing-page-specification)
4. [About Page Specification](#4-about-page-specification)
5. [Content Data](#5-content-data)
6. [Implementation Checklist](#6-implementation-checklist)

---

## 1. Design Decisions Log

This section documents every divergence between the copywriter and UX agent, with the final resolution and rationale.

### DECISION 1: Landing Page Section Order

- **Copywriter proposed:** Hero > TrustBar > Problem > HowItWorks > Frameworks > SocialProof > Brands > GetInTouch
- **UX proposed:** Hero > Problem > TrustBar > HowItWorks > Frameworks > SocialProof > GetInTouch
- **FINAL:** Hero > Problem > TrustBar > HowItWorks > Frameworks > SocialProof > Brands > GetInTouch
- **Rationale:** The UX agent is correct that placing the trust bar between the hero and the problem section creates a "visual wall" before the value proposition lands. The logos gain context after the visitor understands the problem Dataland solves. The Brands (logo wall) section is retained before GetInTouch as the copywriter specified -- it serves a different purpose from the TrustBar (broad social proof vs. curated institutional names).

### DECISION 2: About Page Section Order

- **Copywriter proposed:** Hero > Principles > TrustPillars > Team > AdvisoryBoard > Ecosystem > BottomCTA
- **UX proposed:** Hero > TrustPillars > Team > Principles > AdvisoryBoard > Ecosystem > BottomCTA
- **FINAL:** Hero > TrustPillars > Team > Principles > AdvisoryBoard > Ecosystem > BottomCTA
- **Rationale:** The UX agent is correct. Visitors arriving from the landing page are seeking credentials. Six abstract values between the hero and the trust pillars creates a dense conceptual block. Trust Pillars first, then leadership (who runs this?), then principles (what do they believe?).

### DECISION 3: Video Autoplay Behavior

- **Copywriter proposed:** Autoplay muted loop, self-hosted `<video>` element.
- **UX proposed:** Static thumbnail with centered play button, no autoplay. Lazy-load with IntersectionObserver.
- **FINAL:** Autoplay muted loop. The video is very short (a few seconds), has no narration or sound, and simply shows platform navigation impressions. Autoplay is appropriate here. Lazy-load with IntersectionObserver is still used to defer loading until the section is near-viewport.
- **Rationale:** The video is a short, silent, looping animation -- more like a GIF than a traditional video. Autoplay muted is the correct UX for this type of content. A play button would add unnecessary friction for a few-second clip.

### DECISION 4: Bottom CTA Text

- **Copywriter proposed:** "Join the Movement" as headline.
- **UX proposed:** Use action-specific labels: "Start Using Dataland" and "Talk to Our Team".
- **FINAL:** The About page bottom CTA uses "Talk to Our Team" (primary, opens contact modal) and "Learn More About Our Data" (secondary, navigates to frameworks/pricing). The Landing page gets "Start Using Dataland" (primary, triggers registration) and "Talk to Our Team" (secondary, opens contact modal). Each page's CTAs match the visitor's intent at that point in their journey.
- **Rationale:** "Join the Movement" is vague. The About page is for visitors validating trust -- they are not yet ready to sign up. "Start Using Dataland" belongs on the landing page where conversion intent is higher.

### DECISION 5: Principles Presentation

- **Copywriter proposed:** Card layout (no specific grid).
- **UX proposed:** 3x2 grid (desktop), 2x3 (tablet), 1x6 (mobile). Each card: icon + bold title + single sentence (max 15 words). Subtle orange left border (#ff6813). No accordion.
- **FINAL:** Accept UX recommendation in full.
- **Rationale:** Clear, scannable, visually differentiated from trust pillar cards.

### DECISION 6: Frameworks Section Presentation

- **Copywriter proposed:** Card grid (current pattern).
- **UX proposed:** Tabbed/segmented control using PrimeVue TabView.
- **FINAL:** Keep card grid. Do NOT use TabView.
- **Rationale:** The current card grid is well-tested and fits the established visual language. TabView introduces an interaction pattern not used elsewhere on the landing page and hides content behind clicks. The card grid lets visitors scan all frameworks at once.

### DECISION 7: Hero Search Bar Label

- **UX proposed:** Add a visible label "Search for a company's ESG data" above or below the search bar.
- **FINAL:** Accept. Add a visible `<label>` element above the search input.
- **Rationale:** Improves accessibility (WCAG) and reduces cognitive load for first-time visitors.

### DECISION 8: Responsive Breakpoints

- **Copywriter proposed:** 1440px / 1024px / 768px.
- **UX proposed:** Add 640px. Standardize as SCSS variables: `$bp-sm` (640), `$bp-md` (768), `$bp-lg` (1024), `$bp-xl` (1440).
- **FINAL:** Accept UX recommendation. Add 640px breakpoint. Create SCSS variables file.
- **Rationale:** Small phones (< 640px width) need distinct handling. SCSS variables prevent magic numbers.

### DECISION 9: Contact Modal on Mobile

- **UX proposed:** Consider inline form instead of modal on mobile.
- **FINAL:** Reject. Keep modal on all screen sizes.
- **Rationale:** The contact modal is a shared component (`ContactInquiryModal.vue` + `useContactModal.ts`) used across multiple pages. Splitting behavior by device adds complexity without clear user benefit. The modal is already functional on mobile.

### DECISION 10: Headline Font Size Consistency

- **UX proposed:** Landing page uses 100px headlines vs about page 48px. Bring closer together.
- **FINAL:** Partially accept. Landing page hero headline stays large (100px at xl, scales down). Other landing page section headlines (Problem, Frameworks) are reduced from 100px to 64px at desktop. This brings them closer to the about page's restraint without sacrificing the hero's impact.
- **Rationale:** The hero needs visual punch. Section headlines at 100px compete with it.

### DECISION 11: Accessibility Fixes

- **UX proposed:** Fix "Back" button (make `<button>`), add skip-to-content link, check color contrast.
- **FINAL:** Accept all.
- **Rationale:** These are WCAG compliance requirements, not optional.

### DECISION 12: useBreakpoint Composable

- **UX proposed:** Shared `useBreakpoint` composable instead of scattered resize listeners.
- **FINAL:** Accept. Create `useBreakpoint.ts` composable.
- **Rationale:** Multiple components (TheIntro, TheQuotes, TheHowItWorks) independently attach resize listeners. A shared composable is cleaner and prevents memory leaks.

---

## 2. Global Changes

### 2.1 Responsive Breakpoint Variables

Create a new SCSS file for breakpoint variables.

**File:** `src/assets/scss/breakpoints.scss`

```scss
$bp-sm: 640px;
$bp-md: 768px;
$bp-lg: 1024px;
$bp-xl: 1440px;
```

**Vite config:** Add this file to `css.preprocessorOptions.scss.additionalData` so it is available in all components without explicit import. If `vite.config.ts` does not already have this setting, add:

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

Usage in components (replaces manual resize listeners):

```ts
import { useBreakpoint } from '@/composables/useBreakpoint';
const { isMobile } = useBreakpoint();
// Use in template: v-if="isMobile()"
```

### 2.3 Mobile Navigation -- Hamburger Menu

**File to modify:** `src/components/generics/LandingPageHeader.vue`

Currently the navigation links are `display: none` on mobile (< 768px). Replace with a hamburger menu.

**Behavior:**
- Below `$bp-md` (768px): Hide nav links. Show a hamburger icon button (PrimeVue `Button` with `icon="pi pi-bars"`).
- Clicking hamburger opens a slide-down overlay containing: HOME, ABOUT, LOGIN, SIGN UP (or BACK TO PLATFORM if logged in).
- Clicking any link or a close icon (pi-times) closes the overlay.
- Overlay has `background: rgba(255, 255, 255, 0.96); backdrop-filter: blur(16px);` and slides from top.

**Template structure:**

```html
<header class="header" role="banner">
  <a href="#main-content" class="skip-link">Skip to content</a>
  <div class="header__logo">
    <router-link to="/" aria-label="Go to the Landing Page">
      <img src="/static/logos/gfx_logo_dataland_orange_S.svg" alt="Dataland banner logo" />
    </router-link>
  </div>
  <nav class="header__navigation" :class="{ 'header__navigation--open': menuOpen }">
    <Button ... label="HOME" ... />
    <Button ... label="ABOUT" ... />
  </nav>
  <AuthSection :is-landing-page="true" class="header__auth" :class="{ 'header__auth--hidden': !menuOpen }" />
  <button
    class="header__hamburger"
    :aria-expanded="menuOpen"
    aria-controls="mobile-menu"
    aria-label="Toggle navigation menu"
    @click="menuOpen = !menuOpen"
  >
    <i :class="menuOpen ? 'pi pi-times' : 'pi pi-bars'" />
  </button>
</header>
```

**Key style rules for mobile:**

```scss
@media only screen and (max-width: $bp-md) {
  .header__navigation,
  .header__auth {
    display: none;
  }
  .header__navigation--open,
  .header__auth--hidden.header__auth--visible {
    display: flex;
  }
  .header__hamburger {
    display: flex;
  }
}
@media only screen and (min-width: $bp-md + 1) {
  .header__hamburger {
    display: none;
  }
}
```

### 2.4 Skip-to-Content Link

Add to `LandingPageHeader.vue` template (first child of `<header>`):

```html
<a href="#main-content" class="skip-link">Skip to content</a>
```

Style (visually hidden until focused):

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

Add `id="main-content"` to the `<main>` element in both `LandingPage.vue` and `AboutPage.vue`.

### 2.5 Standardized CTAs

Across the entire site, there are exactly two CTA patterns:

| CTA | Label | Action | Style |
|-----|-------|--------|-------|
| Primary | `Create Free Account` | Keycloak register flow | PrimeVue `Button`, `rounded` |
| Secondary | `Get in Touch` | Opens `ContactInquiryModal` | PrimeVue `Button`, `rounded`, `severity="secondary"` |

The old labels "SIGN UP", "I AM INTERESTED", "START YOUR DATALAND JOURNEY" are all replaced.

---

## 3. Landing Page Specification

### 3.1 Section Order (FINAL)

```
1. TheIntro           (Hero)
2. TheStruggle        (The Problem)
3. TheTrustBar        (NEW -- institutional trust logos)
4. TheDataAccess      (RENAMED from TheHowItWorks)
5. TheFrameworks      (RENAMED from TheJoinCampaign)
6. TheSocialProof     (NEW -- replaces TheQuotes)
7. TheBrands          (Logo wall -- kept)
8. TheGetInTouch      (Contact -- kept)
```

### 3.2 LandingPage.vue Updated Template

**File:** `src/components/pages/LandingPage.vue`

```html
<template>
  <main id="main-content" role="main">
    <TheIntro :sections="landingPage?.sections" />
    <TheStruggle :sections="landingPage?.sections" />
    <TheTrustBar />
    <TheDataAccess :sections="landingPage?.sections" />
    <TheFrameworks :sections="landingPage?.sections" />
    <TheSocialProof :sections="landingPage?.sections" />
    <TheBrands :sections="landingPage?.sections" />
    <TheGetInTouch :sections="landingPage?.sections" />
    <ContactInquiryModal />
  </main>
</template>
```

Update imports accordingly. Remove `TheQuotes` and `TheJoinCampaign` imports. Add `TheTrustBar`, `TheDataAccess`, `TheFrameworks`, `TheSocialProof`.

---

### 3.3 Section: TheIntro (Hero)

**File:** `src/components/resources/landingPage/TheIntro.vue` (MODIFY)

#### Content (from content.json)

- **Headline line 1:** `Liberate Data -`
- **Headline line 2:** `Empower Autonomy.`
- **Subheadline:** `The alternative to data monopolies.`
- **Search label:** `Search for a company's ESG data`
- **Search placeholder:** `Search for a company to view or request data...`

#### Changes from Current

1. **Add visible search label:** Add `<label for="hero-search" class="intro__search-label">Search for a company's ESG data</label>` above the `CompaniesOnlySearchBar`.
2. **Fix Back button:** Change the `<div class="back-button" @click="handleInputBlur">Back</div>` to `<button type="button" class="back-button" @click="handleInputBlur">Back</button>`.
3. **Replace manual resize listener** with `useBreakpoint` composable.
4. **Remove direct DOM manipulation** of `.header` element in `handleInputFocus` / `handleInputBlur`. Instead, emit an event or use a shared reactive state.
5. **CTA buttons below search:** Add two buttons below the search bar:
   - Primary: `Create Free Account` (triggers Keycloak register)
   - Secondary: `Get in Touch` (triggers `openModal` from `useContactModal`)

#### Layout

- Desktop (>= 1440px): Centered, max-width 1007px. Headline 100px/106px line-height.
- Large (1024-1440px): Headline 64px/78px. Max-width 750px.
- Tablet (768-1024px): Headline 48px/56px. Max-width 534px.
- Mobile (< 768px): Headline 40px/48px. Max-width 328px. Padding-inline 16px.
- Small mobile (< 640px): Same as mobile but search bar gets full width, CTA buttons stack vertically.

---

### 3.4 Section: TheStruggle (The Problem)

**File:** `src/components/resources/landingPage/TheStruggle.vue` (MODIFY)

#### Content (from content.json -- unchanged)

- **Headline:** `Dataland aims to fix` (orange) + ` four main data issues`
- **Cards:**
  1. **Data Gaps** -- `Smaller and unlisted companies are often left out of the existing data offering, shifting the burden of acquiring this data to the data consumers.`
  2. **Quality Issues** -- `Inaccurate, inconsistent, or outdated data jeopardizes the integrity of analytics and decision processes, leading to misguided strategies.`
  3. **Usage Restrictions** -- `Companies are quite limited in how they can use the data they acquired, hindering many valuable applications of this data.`
  4. **High Price** -- `Data is becoming more and more expensive, limiting access to essential information for many organizations.`

#### Changes from Current

1. **Reduce headline font-size** from 100px to 64px at desktop (matching about page restraint). Scale: 64px (xl) > 48px (lg) > 40px (md) > 32px (sm).
2. No structural changes. The current 2-column grid with icon + title + text is correct.

#### Layout

- Desktop: 2x2 card grid, max-width 900px centered.
- Tablet: 2x2 grid, narrower padding.
- Mobile (< 768px): 1-column stack.
- Small mobile (< 640px): Same as mobile, reduced icon size.

---

### 3.5 Section: TheTrustBar (NEW)

**File:** `src/components/resources/landingPage/TheTrustBar.vue` (CREATE)

#### Content

- **Label text:** `Trusted by leaders in European financial services`
- **Logos (curated subset):** BVI, VOEB, d-fine, PwC, Werte-Stiftung, Deka, Metzler, BayernLB
  - Use existing logo images from `/static/logos/` directory.

#### Behavior

- Horizontal row of grayscale logos with the label text above.
- On hover, individual logos transition to full color (CSS filter).
- No click action.
- On mobile (< 768px): Logos wrap to 2 rows. Label text is smaller (14px).
- On small mobile (< 640px): 2x4 grid of logos.

#### Template Structure

```html
<section class="trustbar" aria-label="Trusted by">
  <p class="trustbar__label">Trusted by leaders in European financial services</p>
  <div class="trustbar__logos">
    <img v-for="logo in logos" :key="logo.alt" :src="logo.src" :alt="logo.alt"
         class="trustbar__logo" />
  </div>
</section>
```

#### Styles

```scss
.trustbar {
  padding: 48px 32px;
  background: var(--p-surface-0, #ffffff);
  text-align: center;

  &__label {
    font-size: 16px;
    font-weight: 600;
    color: var(--grey-tones-600);
    text-transform: uppercase;
    letter-spacing: 1px;
    margin: 0 0 32px;
  }

  &__logos {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;
    gap: 32px 48px;
    max-width: 1200px;
    margin: 0 auto;
  }

  &__logo {
    height: 40px;
    width: auto;
    filter: grayscale(100%);
    opacity: 0.6;
    transition: filter 0.3s, opacity 0.3s;
    &:hover {
      filter: grayscale(0%);
      opacity: 1;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .trustbar {
    padding: 32px 16px;
    &__label { font-size: 14px; }
    &__logos { gap: 24px 32px; }
    &__logo { height: 32px; }
  }
}
```

#### Data

The logo list is hardcoded in the component (no content.json dependency):

```ts
const logos = [
  { src: '/static/logos/img_bvi.png', alt: 'BVI' },
  { src: '/static/logos/img_voeb.png', alt: 'VOEB' },
  { src: '/static/logos/img_d-fine.png', alt: 'd-fine' },
  { src: '/static/logos/img_pwc.png', alt: 'PwC' },
  { src: '/static/logos/img_wertestiftung.png', alt: 'Werte-Stiftung' },
  { src: '/static/logos/img_deka.png', alt: 'Deka' },
  { src: '/static/logos/img_Metzler.png', alt: 'Metzler' },
  { src: '/static/logos/img_bayernlb.png', alt: 'BayernLB' },
];
```

---

### 3.6 Section: TheDataAccess (RENAMED from TheHowItWorks)

**File:** `src/components/resources/landingPage/TheDataAccess.vue` (CREATE, replaces TheHowItWorks.vue)

#### Content (from content.json "How it works" section)

- **Headline:** `How does Dataland work?`
- **Cards (step cards):**
  1. **Search** -- `Utilize the search feature to find specific companies within the platform.`
  2. **Request company's inclusion** -- `Request company's inclusion in case you can't find the company you are looking for.`
  3. **Request framework data** -- `Request framework data to get the missing data for the frameworks you need.`
  4. **Download** -- `Download either through the platform or through API.`

Note: Card 3 text is updated to remove "campaign" wording. The content.json must reflect this change.

#### Video Integration

Below the step cards, embed a self-hosted video demonstrating the platform.

**Video behavior (per Decision 3):**
- Autoplay, muted, looping. The video is very short (a few seconds), has no narration or sound, and shows impressions of navigating the platform.
- Lazy-load the video source using IntersectionObserver -- do not load until the section is near-viewport.
- Video element: `<video>` with `autoplay`, `muted`, `loop`, `playsinline`, `preload="none"`.
- Source: `/static/videos/platform-demo.mp4` -- **[PLACEHOLDER]** No video file exists yet. The implementation must gracefully show only the poster SVG when the mp4 is missing. Replace with a real screen recording before production.
- Poster frame: `/static/videos/platform-demo-thumb.svg` -- **[PLACEHOLDER]** An SVG mockup of the platform UI is provided. Replace with a real screenshot/webp when the video is produced.

**Template for video area:**

```html
<div class="dataaccess__video" ref="videoContainer">
  <video
    ref="videoEl"
    autoplay
    muted
    loop
    playsinline
    preload="none"
    poster="/static/videos/platform-demo-thumb.svg"
    class="dataaccess__video-player"
  >
    <source :src="videoSrc" type="video/mp4" />
  </video>
</div>
```

**IntersectionObserver pattern (lazy-load only, autoplay handled natively):**

```ts
const videoContainer = ref<HTMLElement | null>(null);
const videoSrc = ref('');
const videoEl = ref<HTMLVideoElement | null>(null);

onMounted(() => {
  if (!videoContainer.value) return;
  const observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        videoSrc.value = '/static/videos/platform-demo.mp4';
        observer.disconnect();
      }
    },
    { rootMargin: '200px' }
  );
  observer.observe(videoContainer.value);
});
```

#### Layout

- Keep the existing horizontal slide/card pattern (SlideShow component).
- Video area: Full section width, max 800px centered, 16:9 aspect ratio, border-radius 16px.
- Background: `var(--p-primary-color)` (orange) -- same as current TheHowItWorks.

---

### 3.7 Section: TheFrameworks (RENAMED from TheJoinCampaign)

**File:** `src/components/resources/landingPage/TheFrameworks.vue` (CREATE, replaces TheJoinCampaign.vue)

#### Content (from content.json "Join a campaign" section -- RENAMED)

The content.json section title changes from `"Join a campaign"` to `"Frameworks"`. All "campaign" wording is removed.

- **Headline:** `Access data across` (black) + ` key ESG frameworks` (orange) + `.`
- **Subheadline:** `Dataland covers the frameworks that matter most for European regulatory compliance and sustainable investing.`
- **CTA button:** `Create Free Account` (primary, triggers Keycloak register)
- **Framework cards:**
  1. **EU Taxonomy** -- `The EU Taxonomy Regulation supports companies to capture their environmentally sustainable economic activities.`
  2. **SFDR** -- `SFDR helps investors to properly assess how sustainability risks are integrated in the investment decision process.`
  3. **PCAF** -- `The PCAF standard provides guidance on how to classify, calculate, and report financed emissions.`
  4. **LkSG** -- `The Lieferkettensorgfalts-pflichtengesetz is a German law requiring companies to monitor human rights and environmental risks in their supply chains.`
  5. **VSME** -- `Captures companies' progress on material sustainability issues, enabling them to respond to capital providers and stakeholders efficiently and proportionally.`

#### Changes from Current TheJoinCampaign

1. Component renamed from `TheJoinCampaign.vue` to `TheFrameworks.vue`.
2. Section title in content.json changed from `"Join a campaign"` to `"Frameworks"`.
3. Headline text updated (no "campaign" language).
4. Subheadline text updated.
5. CTA label changed from `"I AM INTERESTED"` to `"Create Free Account"`.
6. CTA action changed from `openEmailClient` to Keycloak register.
7. Card 3 text updated: "Join a campaign to get the missing data." becomes "Request framework data to get the missing data for the frameworks you need."

#### Layout

- Same card grid as current: 3 columns on desktop, 2 on tablet, 1 on mobile.
- Headline reduced from 100px to 64px at desktop (per Decision 10).

---

### 3.8 Section: TheSocialProof (NEW -- replaces TheQuotes)

**File:** `src/components/resources/landingPage/TheSocialProof.vue` (CREATE)

#### Content

- **Section headline:** `What Our Members Say`

**Sub-section A: Member Quotes** (text-only cards, no YouTube videos):
  1. > "Dataland is the only platform we know that is open to everyone and based on a non-profit business model."
     -- **Sven Schubert**, CEO of Envoria
  2. > "We hope that data availability, coverage and quality is improved, for the benefit of the users, the corporations and society overall."
     -- **Rudolf Siebel**, Managing Director at BVI German Fund Association
  3. > "Dataland can provide the data ecosystem we all need to support our transition to stay within 1.5 degrees or within the planetary boundaries."
     -- **Matthias Kopp**, Director of Sustainable Finance at WWF Germany
  4. > "We appeal to both investors and companies to make sustainability data available in a timely and cost-efficient manner."
     -- **Ingo Speich**, Head of Sustainability & Corporate Governance at Deka Investment

**Sub-section B: Customer Success Story Summaries**

Display 2-3 short success story preview cards. Each card shows a brief summary of a full success story. The full story is on a dedicated page (see section 3.11 below).

Each card contains:
- A title (bold, 20px)
- A 2-3 sentence summary excerpt
- A process sketch/diagram thumbnail (if available)
- A "Read full story" link navigating to `/success-stories/:slug`

**[PLACEHOLDER] Content below is fictional. Replace with real customer stories before production.**

**Story 1:**
- Title: `Closing SFDR Data Gaps for a Mid-Sized Asset Manager`
- Summary: `A German asset manager with EUR 12 billion AuM needed PAI indicator data for over 400 portfolio companies to meet SFDR disclosure deadlines. Dataland provided structured, quality-assured data extracted from public sustainability reports, eliminating weeks of manual research.`
- Link: `/success-stories/sfdr-data-gaps-asset-manager`

**Story 2:**
- Title: `EU Taxonomy Alignment Data for a Regional Bank`
- Summary: `A German regional bank needed EU Taxonomy alignment data for its corporate loan portfolio to calculate its Green Asset Ratio. Dataland delivered structured Taxonomy KPIs extracted from borrowers' public disclosures, enabling the bank to meet EBA Pillar 3 reporting requirements on schedule.`
- Link: `/success-stories/eu-taxonomy-alignment-regional-bank`

**Story 3:**
- Title: `LkSG Supply Chain Due Diligence for an Institutional Investor`
- Summary: `A large German institutional investor used Dataland to assess LkSG compliance indicators across its equity portfolio holdings. The structured due diligence data enabled the investor to identify supply chain risk concentrations and engage proactively with portfolio companies.`
- Link: `/success-stories/lksg-supply-chain-due-diligence-investor`

**Sub-section C: Process Sketch**

- **H3:** `How a Member Uses Dataland`
- Display a visual diagram/sketch illustrating the member workflow as an SVG or image (`/static/images/member-process-sketch.svg`).
- The sketch shows: Member identifies data gap > Submits request > Dataland sources from public reports (AI + human review) > Structured data available > Member downloads or accesses via API.

#### Changes from TheQuotes

The old TheQuotes component embedded YouTube videos via the YouTube IFrame API, with cookie consent handling and complex slide management. This is entirely replaced:

1. **No YouTube embeds.** All YouTube-related code is removed.
2. **No cookie consent dependency** for this section.
3. Text-based testimonial cards + success story summaries + process sketch.
4. Clean, lightweight implementation.

#### Layout

**Quote cards:**
- Desktop: 2x2 grid, max-width 1000px centered.
- Tablet: 2-column grid.
- Mobile: Horizontal SlideShow (reuse existing `SlideShow.vue` component) with arrow navigation.

**Success story cards:**
- Desktop: 3 columns, gap 32px.
- Tablet: 2 columns (third wraps).
- Mobile: Single column, gap 16px.

**Process sketch:**
- Full width, max-width 900px centered.
- Mobile: horizontally scrollable if wider than viewport, or stacked vertically.

#### Card Design

**Quote cards:**
- White background, border-radius 16px, subtle shadow (`box-shadow: 0 4px 32px 0 rgba(0,0,0,0.08)`).
- Large opening quotation mark (decorative, `font-size: 48px; color: var(--p-primary-color); opacity: 0.3;`).
- Quote text: `font-size: 20px; line-height: 28px; font-weight: 400;`.
- Attribution: `font-size: 14px; font-weight: 600;` for name, `font-weight: 400; color: var(--p-primary-color);` for role.
- Padding: 40px.

**Success story cards:**
- White background, border-radius 12px, border 1px solid `var(--grey-tones-200)`.
- Title: `font-size: 20px; font-weight: 600;`.
- Summary: `font-size: 16px; color: var(--grey-tones-600); line-height: 24px;`.
- "Read full story" link: `font-size: 14px; font-weight: 600; color: var(--p-primary-color);`.
- Padding: 32px.

#### Data

Store quotes and success story summaries in `socialProofContent.ts` (new file, following `aboutContent.ts` pattern).

---

### 3.11 Customer Success Stories Page (NEW)

**Route:** `/success-stories/:slug`
**File:** `src/components/pages/SuccessStoryPage.vue` (CREATE)

#### Purpose

Each customer success story gets its own dedicated page, accessible from the landing page summary cards. This separates the brief teaser (on the landing page) from the full narrative.

#### Route Registration

Add to `src/router/index.ts`:

```ts
{
  path: '/success-stories/:slug',
  name: 'success-story',
  component: () => import('@/components/pages/SuccessStoryPage.vue'),
  meta: { layout: 'landing' },
}
```

#### Page Structure

Each success story page contains:
1. **Header:** Same `LandingPageHeader` as landing/about pages.
2. **Hero section:** Story title + company type (e.g., "Mid-Sized Asset Manager") + framework tag (e.g., "SFDR").
3. **The Challenge:** What data problem the member faced.
4. **The Process:** How they used Dataland to solve it, with a process diagram/sketch specific to their workflow.
5. **The Result:** Outcome -- data delivered, timeline, how it was used.
6. **Pull quote:** A quote from the member about their experience.
7. **CTA:** "Start Using Dataland" (registration) + "Read More Stories" (back to landing page social proof section).
8. **Footer:** Same footer as landing/about pages.

#### Content Data

Store success stories in `src/components/resources/successStories/successStoryContent.ts`:

```ts
export interface SuccessStory {
  slug: string;
  title: string;
  summary: string;
  companyType: string;
  framework: string;
  challenge: string;
  process: string;
  result: string;
  quote?: {
    text: string;
    attribution: string;
    role: string;
  };
  processSketchPath?: string;
}

// [PLACEHOLDER] All names, companies, quotes, and figures below are fictional.
// Replace with real customer stories before production launch.
export const SUCCESS_STORIES: SuccessStory[] = [
  {
    slug: 'sfdr-data-gaps-asset-manager',
    title: 'Closing SFDR Data Gaps for a Mid-Sized Asset Manager',
    summary:
      'A German asset manager with EUR 12 billion AuM needed PAI indicator data for over 400 portfolio companies to meet SFDR disclosure deadlines. Dataland provided structured, quality-assured data extracted from public sustainability reports, eliminating weeks of manual research.',
    companyType: 'Mid-sized asset manager',
    framework: 'SFDR',
    challenge:
      'With SFDR Level 2 requirements in full effect, the firm faced a critical data gap: PAI indicators were missing for nearly 60% of their portfolio holdings, particularly among small- and mid-cap European companies. Commercial ESG data providers covered the large caps, but the long tail of smaller holdings remained a blind spot. The compliance team estimated that sourcing this data manually from published sustainability reports would require over 800 person-hours per reporting cycle. The deadline for the annual PAI statement was approaching, and the team lacked both the capacity and the specialized knowledge to extract the required data points from diverse report formats.',
    process:
      'The firm submitted a batch request through Dataland, uploading a list of 420 portfolio companies with missing SFDR PAI data. Dataland\'s AI extraction engine processed publicly available sustainability reports, annual reports, and non-financial disclosures for each company, mapping extracted data points to the 18 mandatory PAI indicators. A human quality assurance review was conducted on all AI-extracted data before publication. The structured datasets were made available on the platform within three weeks, and the firm downloaded the complete dataset via Dataland\'s API for direct integration into their PAI statement generation workflow.',
    result:
      'The firm achieved 94% PAI indicator coverage across its portfolio, up from 40% before using Dataland. The entire process -- from data request to API export -- took 22 days, compared to an estimated 10 weeks of manual effort. The compliance team reported that the structured format reduced downstream processing time by approximately 70%.',
    quote: {
      text: 'We went from dreading PAI reporting season to having a reliable, repeatable process. Dataland gave us coverage where no commercial provider could, and the data quality was excellent.',
      attribution: 'Placeholder Name',
      role: 'Head of ESG Compliance, Placeholder Asset Management GmbH',
    },
    processSketchPath: '/static/images/process-sketch-sfdr.svg',
  },
  {
    slug: 'eu-taxonomy-alignment-regional-bank',
    title: 'EU Taxonomy Alignment Data for a Regional Bank',
    summary:
      'A German regional bank needed EU Taxonomy alignment data for its corporate loan portfolio to calculate its Green Asset Ratio. Dataland delivered structured Taxonomy KPIs extracted from borrowers\' public disclosures, enabling the bank to meet EBA Pillar 3 reporting requirements on schedule.',
    companyType: 'Regional bank',
    framework: 'EU Taxonomy',
    challenge:
      'As a credit institution subject to CRR requirements, the bank needed to calculate and disclose its Green Asset Ratio (GAR) under the EU Taxonomy regulation. This required Taxonomy alignment data -- specifically revenue, CapEx, and OpEx KPIs -- for over 300 corporate borrowers in its loan book. Most of these borrowers were mid-market German companies that did not proactively share structured Taxonomy data with their lenders. The bank had attempted a manual outreach approach, sending questionnaires to borrowers, but response rates were below 15%. Without reliable alignment data, the bank faced the prospect of reporting near-zero GAR figures despite having a portfolio with significant exposure to climate-relevant economic activities.',
    process:
      'The bank provided Dataland with a list of corporate borrowers and the specific Taxonomy KPIs needed for GAR calculation. Dataland\'s platform identified and retrieved publicly available annual reports, non-financial statements, and Taxonomy-specific disclosures for each company. The AI extraction engine parsed these documents to identify reported Taxonomy-eligible and Taxonomy-aligned revenue, CapEx, and OpEx figures, along with the underlying economic activities and environmental objectives. Each extracted dataset was reviewed by Dataland\'s QA team for accuracy and completeness. The verified data was delivered via API in a format directly compatible with the bank\'s regulatory reporting system.',
    result:
      'The bank obtained Taxonomy alignment data for 78% of its corporate loan portfolio by value, transforming its GAR disclosure from a near-zero placeholder to a meaningful metric. The data was delivered six weeks ahead of the EBA reporting deadline, giving the bank\'s risk and compliance teams sufficient time for internal validation and board-level review.',
    quote: {
      text: 'Our borrower questionnaire approach was simply not working. Dataland allowed us to source Taxonomy data from public disclosures at scale -- something we could not have done internally without a dedicated team.',
      attribution: 'Placeholder Name',
      role: 'Head of Regulatory Reporting, Placeholder Landesbank',
    },
    processSketchPath: '/static/images/process-sketch-eu-taxonomy.svg',
  },
  {
    slug: 'lksg-supply-chain-due-diligence-investor',
    title: 'LkSG Supply Chain Due Diligence for an Institutional Investor',
    summary:
      'A large German institutional investor used Dataland to assess LkSG compliance indicators across its equity portfolio holdings. The structured due diligence data enabled the investor to identify supply chain risk concentrations and engage proactively with portfolio companies.',
    companyType: 'Institutional investor',
    framework: 'LkSG',
    challenge:
      'Under the German Supply Chain Due Diligence Act (LkSG), the institutional investor -- a pension fund with EUR 45 billion in assets -- needed to understand the human rights and environmental due diligence practices of companies in its equity portfolio. While the investor was not directly subject to LkSG obligations, its board had committed to voluntary alignment with LkSG standards as part of its responsible investment policy. The portfolio included over 250 German and European companies, many of which had complex global supply chains. Assessing each company\'s grievance mechanisms, risk analysis processes, preventive measures, and remedial actions from published reports was a task far beyond the capacity of the three-person ESG integration team.',
    process:
      'The investor submitted its portfolio holdings to Dataland and requested LkSG-relevant due diligence data for each company. Dataland\'s AI engine analyzed published sustainability reports, human rights policy documents, supply chain disclosures, and BAFA-related public statements. The extraction focused on the core LkSG requirements: risk analysis methodology, preventive and remedial measures, grievance mechanisms, and documentation practices. All data points were mapped to a structured LkSG framework and reviewed by Dataland\'s quality assurance team. The final dataset was delivered through the platform\'s download function, with each company\'s data linked to the source document for full traceability.',
    result:
      'The investor received structured LkSG due diligence assessments for 230 of its 250 portfolio companies within four weeks. The data revealed that 35% of assessed companies had incomplete or missing grievance mechanisms -- a finding that directly informed the investor\'s engagement priorities for the following year. The ESG integration team estimated that the Dataland-sourced data saved approximately 1,200 hours of analyst time.',
    quote: {
      text: 'LkSG compliance data was a black box for us before Dataland. Now we have a structured, source-linked dataset that our portfolio managers actually use in their engagement conversations.',
      attribution: 'Placeholder Name',
      role: 'Head of ESG Integration, Placeholder Pensionskasse',
    },
    processSketchPath: '/static/images/process-sketch-lksg.svg',
  },
];
```

#### Mobile Support

Full responsive design following the same breakpoint strategy as the landing and about pages. Content stacks vertically on mobile.

#### Implementation Note

This page ships with placeholder content (fictional names, companies, and figures -- all marked with `[PLACEHOLDER]` comments in the code). The structure is ready for the content team to swap in real stories. All 3 placeholder stories should be displayed in the initial implementation -- no `v-if` hiding needed since placeholder content is provided.

---

### 3.9 Section: TheBrands (Logo Wall -- KEEP)

**File:** `src/components/resources/landingPage/TheBrands.vue` (MINOR MODIFY)

#### Changes

1. No structural changes.
2. Update headline copy if desired (current: "Already a trusted partner for top companies" -- keep as-is).
3. Ensure responsive breakpoints use SCSS variables.

---

### 3.10 Section: TheGetInTouch (Contact -- MODIFY)

**File:** `src/components/resources/landingPage/TheGetInTouch.vue` (MODIFY)

#### Content

- **Headline:** `Get in touch`
- **Contact person:** Dr. Moritz Kiese, Managing Director of Dataland
- **CTA button label:** `Get in Touch` (opens ContactInquiryModal)

#### Changes from Current

1. CTA label changed from `"GET IN TOUCH"` (all caps) to `"Get in Touch"` (title case) for consistency.
2. No structural changes.

---

## 4. About Page Specification

### 4.1 Section Order (FINAL)

```
1. TheAboutHero          (MODIFY)
2. TheAboutTrustPillars  (MODIFY)
3. TheAboutTeam          (KEEP)
4. TheAboutPrinciples    (NEW)
5. TheAboutAdvisoryBoard (MODIFY)
6. TheAboutEcosystem     (NEW -- merges Sponsors + Partners)
7. TheAboutBottomCTA     (MODIFY)
```

### 4.2 AboutPage.vue Updated Template

**File:** `src/components/pages/AboutPage.vue`

```html
<template>
  <main id="main-content" role="main">
    <ContactInquiryModal />
    <TheAboutHero />
    <TheAboutTrustPillars />
    <TheAboutTeam />
    <TheAboutPrinciples />
    <TheAboutAdvisoryBoard />
    <TheAboutEcosystem />
    <TheAboutBottomCTA />
  </main>
</template>
```

Remove `TheAboutSponsors` and `TheAboutPartners` imports. Add `TheAboutPrinciples` and `TheAboutEcosystem`.

---

### 4.3 Section: TheAboutHero (MODIFY)

**File:** `src/components/resources/aboutPage/TheAboutHero.vue`

#### Content (from aboutContent.ts HERO_COPY)

- **Headline:** `Who Stands Behind Dataland`
- **Subheadline:** `A non-profit ESG data platform owned by a charitable foundation, backed by institutional leaders in German financial services.`
- **CTA:** `Get in Touch` (opens ContactInquiryModal -- keep current behavior)

#### Changes

No content changes. Ensure `id="main-content"` is NOT on this section (it goes on `<main>`).

---

### 4.4 Section: TheAboutTrustPillars (MODIFY)

**File:** `src/components/resources/aboutPage/TheAboutTrustPillars.vue`

#### Content (from aboutContent.ts TRUST_PILLARS)

Update the fourth pillar per copywriter spec:

| # | Icon | Title | Description |
|---|------|-------|-------------|
| 1 | `pi pi-lock` | Cannot be sold | 100% owned by Werte-Stiftung, a Frankfurt charitable foundation. Non-commercial by structure, not just by policy. |
| 2 | `pi pi-shield` | Institutionally backed | Backed by d-fine, PwC, and the leadership of BVI and VOEB -- established names in German financial services. |
| 3 | `pi pi-chart-bar` | Narrow scope | Makes published sustainability data accessible. No ratings, no assessments, no commercial agenda. |
| 4 | `pi pi-microchip` | Transparent Technology | Human-supervised AI extraction applied to public company disclosures -- always with expert review, always with full traceability. |

**CHANGE:** Pillar 4 title changes from `"AI at non-profit scale"` to `"Transparent Technology"`. Description updated to include human-in-the-loop messaging and remove "inspect the code".

Update `aboutContent.ts` accordingly.

---

### 4.5 Section: TheAboutTeam (KEEP)

**File:** `src/components/resources/aboutPage/TheAboutTeam.vue`

No changes. Current implementation is correct. Data stays in `aboutContent.ts` as `LEADERSHIP_TEAM`.

---

### 4.6 Section: TheAboutPrinciples (NEW)

**File:** `src/components/resources/aboutPage/TheAboutPrinciples.vue` (CREATE)

#### Content (from content.json about page principles)

- **Section headline:** `Our Principles`
- **Cards (6 total):**

| # | Icon | Title | Description |
|---|------|-------|-------------|
| 1 | `pi pi-verified` | Integrity | We need comparable and reliable sustainability data to create value. |
| 2 | `pi pi-eye` | Disclosure | We seek disclosure of sustainability data from our business relations. |
| 3 | `pi pi-unlock` | Transparency | We respect and promote data sovereignty. |
| 4 | `pi pi-check-circle` | Accountability | Data should be timely and easily accessible at fair cost. |
| 5 | `pi pi-balance-scale` | Neutrality | Common data spaces should be neutral, transparent, non-competitive and not-for-profit. |
| 6 | `pi pi-users` | Collaboration | We work together to achieve these principles and promote their acceptance. |

Note: Typos from the original content.json are fixed ("soveignty" -> "sovereignty", "easliy" -> "easily").

#### Layout (per Decision 5)

- Desktop (>= 1024px): 3x2 grid.
- Tablet (768-1024px): 2x3 grid.
- Mobile (< 768px): 1x6 stack.

#### Card Design

Each card is visually distinct from TrustPillarCards:
- Left border: 3px solid `#ff6813` (orange).
- Background: `var(--p-surface-0, #ffffff)`.
- Border: 1px solid `var(--p-surface-200, #dadada)` on top/right/bottom.
- Border-radius: 0.75rem.
- Padding: 1.5rem.
- Icon: 1.5rem, color `#ff6813`.
- Title: `font-size: 1.125rem; font-weight: 700;`.
- Description: `font-size: 1rem; font-weight: 400; color: var(--p-text-muted-color); line-height: 1.6;` Max 15 words (already satisfied by current copy).
- No accordion/collapsible behavior.

#### Template

```html
<section class="about-principles" role="region" aria-labelledby="principles-heading">
  <div class="about-principles__wrapper">
    <h2 id="principles-heading" class="about-principles__heading">Our Principles</h2>
    <div class="about-principles__grid">
      <div v-for="principle in PRINCIPLES" :key="principle.title" class="about-principles__card">
        <i :class="principle.icon" aria-hidden="true" class="about-principles__card-icon" />
        <h3 class="about-principles__card-title">{{ principle.title }}</h3>
        <p class="about-principles__card-description">{{ principle.description }}</p>
      </div>
    </div>
  </div>
</section>
```

#### Data (in aboutContent.ts)

```ts
export interface Principle {
  icon: string;
  title: string;
  description: string;
}

export const PRINCIPLES: Principle[] = [
  { icon: 'pi pi-verified', title: 'Integrity', description: 'We need comparable and reliable sustainability data to create value.' },
  { icon: 'pi pi-eye', title: 'Disclosure', description: 'We seek disclosure of sustainability data from our business relations.' },
  { icon: 'pi pi-unlock', title: 'Transparency', description: 'We respect and promote data sovereignty.' },
  { icon: 'pi pi-check-circle', title: 'Accountability', description: 'Data should be timely and easily accessible at fair cost.' },
  { icon: 'pi pi-balance-scale', title: 'Neutrality', description: 'Common data spaces should be neutral, transparent, non-competitive and not-for-profit.' },
  { icon: 'pi pi-users', title: 'Collaboration', description: 'We work together to achieve these principles and promote their acceptance.' },
];
```

---

### 4.7 Section: TheAboutAdvisoryBoard (MODIFY)

**File:** `src/components/resources/aboutPage/TheAboutAdvisoryBoard.vue`

#### Content (from aboutContent.ts ADVISORY_BOARD)

Per copywriter spec: Show name + company name + link only. No organisation descriptions.

Current data already has: name, role, organisation, imagePath. The `PersonCard` component currently shows the organisation text. This is correct -- no change needed to the data or display.

**One change:** If the advisory board members should link to their company websites, add an optional `url` field to `AdvisoryPerson`:

```ts
export interface AdvisoryPerson {
  name: string;
  role: string;
  organisation: string;
  imagePath: string;
  url?: string;
}
```

Update data:

```ts
export const ADVISORY_BOARD: AdvisoryPerson[] = [
  {
    name: 'Rudi Siebel',
    role: 'Advisory Board Member',
    organisation: 'BVI',
    imagePath: '/static/about/team-rudi-siebel.webp',
    url: 'https://www.bvi.de',
  },
  {
    name: 'Stephan Henkel',
    role: 'Advisory Board Member',
    organisation: 'VOEB',
    imagePath: '/static/about/team-stephan-henkel.webp',
    url: 'https://www.voeb.de',
  },
];
```

Update `PersonCard.vue` to render the organisation as a link when `url` is provided.

---

### 4.8 Section: TheAboutEcosystem (NEW -- merges Sponsors + Partners)

**File:** `src/components/resources/aboutPage/TheAboutEcosystem.vue` (CREATE)

#### Content

- **Section headline:** `Our Ecosystem`
- **Subsection 1 label:** `Sponsors`
- **Sponsors logos:** T-Systems, d-fine, PwC, Experience One (same images as current)
- **Subsection 2 label:** `Partners`
- **Partners logos:** Eskua AI, Keynum, FACT First Cloud, Sopra Steria (same images as current)

#### Layout

Single section with two labeled subsections. Each subsection shows a horizontal row of logos.

- Desktop: Logos in a single row per subsection.
- Tablet: Logos wrap to multiple rows.
- Mobile: 2 logos per row.

#### Template

```html
<section class="about-ecosystem" role="region" aria-labelledby="ecosystem-heading">
  <div class="about-ecosystem__wrapper">
    <h2 id="ecosystem-heading" class="about-ecosystem__heading">Our Ecosystem</h2>

    <div class="about-ecosystem__group">
      <h3 class="about-ecosystem__group-label">Sponsors</h3>
      <div class="about-ecosystem__logos">
        <LogoChip v-for="logo in SPONSORS" :key="logo.name" :logo="logo" />
      </div>
    </div>

    <div class="about-ecosystem__group">
      <h3 class="about-ecosystem__group-label">Partners</h3>
      <div class="about-ecosystem__logos">
        <LogoChip v-for="logo in PARTNERS" :key="logo.name" :logo="logo" />
      </div>
    </div>
  </div>
</section>
```

#### Styles

```scss
.about-ecosystem {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 3rem;
  }

  &__heading {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0;
  }

  &__group {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  &__group-label {
    font-size: 1.125rem;
    font-weight: 600;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  &__logos {
    display: flex;
    flex-wrap: wrap;
    gap: 2rem 3rem;
    justify-content: flex-start;
    align-items: center;
  }
}

@media only screen and (max-width: $bp-md) {
  .about-ecosystem {
    padding: 2.5rem 1rem;
  }
}
```

---

### 4.9 Section: TheAboutBottomCTA (MODIFY)

**File:** `src/components/resources/aboutPage/TheAboutBottomCTA.vue`

#### Content (updated per Decision 4)

The About page is for visitors validating trust -- they are not yet ready to register. CTAs should guide toward conversation, not conversion.

Update `aboutContent.ts` BOTTOM_CTA_COPY:

```ts
export const BOTTOM_CTA_COPY = {
  headline: 'Let Us Start the Conversation',
  subheadline:
    'Whether you want to consume data, contribute data, or support the platform as a sponsor -- there is a place for you.',
  primaryCtaLabel: 'Talk to Our Team',
  secondaryCtaLabel: 'Learn More About Our Data',
};
```

#### Changes

Add a second CTA button. The template becomes:

```html
<div class="about-bottom-cta__actions">
  <Button :label="BOTTOM_CTA_COPY.primaryCtaLabel" rounded @click="openModal" />
  <Button :label="BOTTOM_CTA_COPY.secondaryCtaLabel" rounded severity="secondary" @click="navigateToFrameworks" />
</div>
```

Wire `openModal` to ContactInquiryModal (existing pattern). Wire `navigateToFrameworks` to scroll to the Frameworks section on the landing page:

```ts
import { useRouter } from 'vue-router';
const router = useRouter();
const navigateToFrameworks = (): void => {
  void router.push({ path: '/', hash: '#frameworks' });
};
```

**Note:** "Start Using Dataland" (registration CTA) belongs on the **landing page** GetInTouch section and hero, not on the About page.

---

## 5. Content Data

### 5.1 content.json Updates

**File:** `src/assets/content.json`

Changes to make:

1. **"How it works" section, card 3:** Change `"text": "Join a campaign to get the missing data."` to `"text": "Request framework data to get the missing data for the frameworks you need."`

2. **"Join a campaign" section:** Rename `"title"` from `"Join a campaign"` to `"Frameworks"`. Update headline text array:
   - From: `["Join a campaign to", " get the missing ", "data.", "The campaigns are..."]`
   - To: `["Access data across", " key ESG frameworks", ".", "Dataland covers the frameworks that matter most for European regulatory compliance and sustainable investing."]`

3. **"Quotes" section:** Replace entirely with social proof quotes (text-only):
   - Rename `"title"` from `"Quotes"` to `"Social Proof"`.
   - Replace cards with text-only quote cards (no YouTube video IDs):
   ```json
   {
     "title": "Social Proof",
     "text": ["What Our Community Says"],
     "cards": [
       {
         "title": "Sven Schubert",
         "text": "CEO of Envoria",
         "date": "\"Dataland is the only platform we know that is open to everyone and based on a non-profit business model.\""
       },
       {
         "title": "Dr. Egbert Schark",
         "text": "Founder and Managing Director at d-fine GmbH",
         "date": "\"I believe the fascinating idea is worth supporting. Join the mission! Join Dataland!\""
       },
       {
         "title": "Dr. Anna-Lisa Schwarz",
         "text": "Managing Director at Werte-Stiftung",
         "date": "\"Dataland will help solve data issues by ensuring transparent, open and fair access to sustainability data.\""
       },
       {
         "title": "Rudolf Siebel",
         "text": "Managing Director at BVI German Fund Association",
         "date": "\"We hope that data availability, coverage and quality is improved, for the benefit of the users, the corporations and society overall.\""
       }
     ]
   }
   ```

4. **About page "Our principles" section:** Fix typos:
   - `"data soveignty"` -> `"data sovereignty"`
   - `"easliy"` -> `"easily"`

5. **"Claim" section:** Remove this section entirely (it is not rendered by any component).

### 5.2 aboutContent.ts Updates

**File:** `src/components/resources/aboutPage/aboutContent.ts`

Full updated file contents:

```ts
export interface TrustPillar {
  icon: string;
  title: string;
  description: string;
}

export interface Person {
  name: string;
  role: string;
  bio: string;
  imagePath: string;
}

export interface AdvisoryPerson {
  name: string;
  role: string;
  organisation: string;
  imagePath: string;
  url?: string;
}

export interface Logo {
  name: string;
  imagePath: string;
}

export interface Principle {
  icon: string;
  title: string;
  description: string;
}

export const TRUST_PILLARS: TrustPillar[] = [
  {
    icon: 'pi pi-lock',
    title: 'Cannot be sold',
    description:
      '100% owned by Werte-Stiftung, a Frankfurt charitable foundation. Non-commercial by structure, not just by policy.',
  },
  {
    icon: 'pi pi-shield',
    title: 'Institutionally backed',
    description:
      'Backed by d-fine, PwC, and the leadership of BVI and VOEB — established names in German financial services.',
  },
  {
    icon: 'pi pi-chart-bar',
    title: 'Narrow scope',
    description:
      'Makes published sustainability data accessible. No ratings, no assessments, no commercial agenda.',
  },
  {
    icon: 'pi pi-microchip',
    title: 'Transparent Technology',
    description:
      'Human-supervised AI extraction applied to public company disclosures — always with expert review, always with full traceability.',
  },
];

export const LEADERSHIP_TEAM: Person[] = [
  {
    name: 'Moritz Kiese',
    role: 'Managing Director',
    bio: 'Moritz leads Dataland with a focus on open-source sustainability infrastructure and European ESG data standards.',
    imagePath: '/static/images/Moritz_Kiese.jpg',
  },
  {
    name: 'Andreas Höcherl',
    role: 'Head of Product',
    bio: 'Andreas shapes the product vision, working closely with regulatory stakeholders and institutional members.',
    imagePath: '/static/about/team-andreas-hoecherl.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
  },
  {
    name: 'Sören Vorsmann',
    role: 'Head of Operations',
    bio: 'Sören oversees platform operations, infrastructure, and member onboarding.',
    imagePath: '/static/about/team-soeren-vorsmann.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
  },
];

export const ADVISORY_BOARD: AdvisoryPerson[] = [
  {
    name: 'Rudi Siebel',
    role: 'Advisory Board Member',
    organisation: 'BVI',
    imagePath: '/static/about/team-rudi-siebel.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
    url: 'https://www.bvi.de',
  },
  {
    name: 'Stephan Henkel',
    role: 'Advisory Board Member',
    organisation: 'VOEB',
    imagePath: '/static/about/team-stephan-henkel.svg', // [PLACEHOLDER] SVG avatar with initials. Replace with real photo.
    url: 'https://www.voeb.de',
  },
];

export const SPONSORS: Logo[] = [
  { name: 'T-Systems', imagePath: '/static/logos/img_t_systems.png' },
  { name: 'd-fine', imagePath: '/static/logos/img_d-fine.png' },
  { name: 'PwC', imagePath: '/static/logos/img_pwc.png' },
  { name: 'Experience One', imagePath: '/static/logos/img_Experience_One.png' },
];

export const PARTNERS: Logo[] = [
  { name: 'Eskua AI', imagePath: '/static/about/logo-eskua-ai.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
  { name: 'Keynum', imagePath: '/static/about/logo-keynum.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
  { name: 'FACT First Cloud', imagePath: '/static/about/logo-fact-first-cloud.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
  { name: 'Sopra Steria', imagePath: '/static/about/logo-sopra-steria.svg' }, // [PLACEHOLDER] SVG text logo. Replace with real logo.
];

export const PRINCIPLES: Principle[] = [
  {
    icon: 'pi pi-verified',
    title: 'Integrity',
    description: 'We need comparable and reliable sustainability data to create value.',
  },
  {
    icon: 'pi pi-eye',
    title: 'Disclosure',
    description: 'We seek disclosure of sustainability data from our business relations.',
  },
  {
    icon: 'pi pi-unlock',
    title: 'Transparency',
    description: 'We respect and promote data sovereignty.',
  },
  {
    icon: 'pi pi-check-circle',
    title: 'Accountability',
    description: 'Data should be timely and easily accessible at fair cost.',
  },
  {
    icon: 'pi pi-balance-scale',
    title: 'Neutrality',
    description:
      'Common data spaces should be neutral, transparent, non-competitive and not-for-profit.',
  },
  {
    icon: 'pi pi-users',
    title: 'Collaboration',
    description: 'We work together to achieve these principles and promote their acceptance.',
  },
];

export const HERO_COPY = {
  headline: 'Who Stands Behind Dataland',
  subheadline:
    'A non-profit ESG data platform owned by a charitable foundation, backed by institutional leaders in German financial services.',
  ctaLabel: 'Get in Touch',
};

export const BOTTOM_CTA_COPY = {
  headline: 'Let Us Start the Conversation',
  subheadline:
    'Whether you want to consume data, contribute data, or support the platform as a sponsor -- there is a place for you.',
  primaryCtaLabel: 'Talk to Our Team',
  secondaryCtaLabel: 'Learn More About Our Data',
};
```

---

## 6. Implementation Checklist

### 6.1 Files to CREATE

| File | Description |
|------|-------------|
| `src/assets/scss/breakpoints.scss` | SCSS breakpoint variables ($bp-sm, $bp-md, $bp-lg, $bp-xl) |
| `src/composables/useBreakpoint.ts` | Shared reactive breakpoint composable |
| `src/components/resources/landingPage/TheTrustBar.vue` | NEW: Institutional trust bar with grayscale logos |
| `src/components/resources/landingPage/TheDataAccess.vue` | NEW: Replaces TheHowItWorks with video integration |
| `src/components/resources/landingPage/TheFrameworks.vue` | NEW: Replaces TheJoinCampaign, no "campaign" wording |
| `src/components/resources/landingPage/TheSocialProof.vue` | NEW: Text-based testimonials replacing YouTube quotes |
| `src/components/resources/landingPage/socialProofContent.ts` | NEW: Content data for TheSocialProof (quotes + success story summaries) |
| `src/components/resources/aboutPage/TheAboutPrinciples.vue` | NEW: 6-card principles grid with orange left border |
| `src/components/resources/aboutPage/TheAboutEcosystem.vue` | NEW: Merged sponsors + partners section |
| `src/components/pages/SuccessStoryPage.vue` | NEW: Dedicated page for full customer success stories |
| `src/components/resources/successStories/successStoryContent.ts` | NEW: Content data for success stories (slug, title, challenge, process, result, quote) |

### 6.2 Files to MODIFY

| File | Changes |
|------|---------|
| `src/components/pages/LandingPage.vue` | Update section order, imports. Add `id="main-content"` to `<main>`. |
| `src/components/pages/AboutPage.vue` | Update section order, imports. Remove Sponsors/Partners, add Principles/Ecosystem. Add `id="main-content"`. |
| `src/components/generics/LandingPageHeader.vue` | Add hamburger menu, skip-to-content link, mobile nav overlay. |
| `src/components/resources/landingPage/TheIntro.vue` | Add search label, fix Back button to `<button>`, add CTA buttons, use `useBreakpoint`, remove DOM manipulation. |
| `src/components/resources/landingPage/TheStruggle.vue` | Reduce headline from 100px to 64px at desktop. |
| `src/components/resources/landingPage/TheGetInTouch.vue` | Update CTA label to title case "Get in Touch". |
| `src/components/resources/landingPage/TheBrands.vue` | Use SCSS breakpoint variables (minor). |
| `src/components/resources/aboutPage/aboutContent.ts` | Update trust pillar 4 title/description. Add Principle interface + PRINCIPLES array. Update BOTTOM_CTA_COPY with dual CTA labels. Add url field to AdvisoryPerson. |
| `src/components/resources/aboutPage/TheAboutTrustPillars.vue` | No template changes; data change flows from aboutContent.ts. |
| `src/components/resources/aboutPage/TheAboutAdvisoryBoard.vue` | No major changes; PersonCard handles url if added. |
| `src/components/resources/aboutPage/TheAboutBottomCTA.vue` | Add second CTA button (primary = register, secondary = contact). Wire register to Keycloak. |
| `src/components/resources/aboutPage/PersonCard.vue` | Add optional link rendering for advisory board organisation when url is present. |
| `src/assets/content.json` | Rename sections, update copy, remove "Claim" section, fix typos. See Section 5.1. |
| `src/router/index.ts` | Add `/success-stories/:slug` route pointing to SuccessStoryPage.vue. |
| `src/types/ContentTypes.ts` | No changes needed (existing Card interface is flexible enough). |

### 6.3 Files to DELETE (after new components are in place)

| File | Reason |
|------|--------|
| `src/components/resources/landingPage/TheQuotes.vue` | Replaced by TheSocialProof.vue |
| `src/components/resources/landingPage/TheHowItWorks.vue` | Replaced by TheDataAccess.vue |
| `src/components/resources/landingPage/TheJoinCampaign.vue` | Replaced by TheFrameworks.vue |
| `src/components/resources/aboutPage/TheAboutSponsors.vue` | Merged into TheAboutEcosystem.vue |
| `src/components/resources/aboutPage/TheAboutPartners.vue` | Merged into TheAboutEcosystem.vue |

### 6.4 Assets Needed

| Asset | Path | Notes |
|-------|------|-------|
| Platform demo video | `/static/videos/platform-demo.mp4` | **[PLACEHOLDER: NOT YET CREATED]** Self-hosted, short (few seconds), silent, looping. Implementation must gracefully show only poster SVG when mp4 is missing. Replace with real screen recording before production. |
| Platform demo poster | `/static/videos/platform-demo-thumb.svg` | **[PLACEHOLDER: SVG provided]** Mockup of platform UI. Replace with real screenshot when video is produced. |
| Member process sketch | `/static/images/member-process-sketch.svg` | **[PLACEHOLDER: SVG provided]** Generic member workflow flow diagram. Replace with professionally designed version. |
| SFDR process sketch | `/static/images/process-sketch-sfdr.svg` | **[PLACEHOLDER: SVG provided]** SFDR-specific flow for success story. Replace with designed version. |
| EU Taxonomy process sketch | `/static/images/process-sketch-eu-taxonomy.svg` | **[PLACEHOLDER: SVG provided]** EU Taxonomy-specific flow for success story. Replace with designed version. |
| LkSG process sketch | `/static/images/process-sketch-lksg.svg` | **[PLACEHOLDER: SVG provided]** LkSG-specific flow for success story. Replace with designed version. |
| Team photo: Andreas Hoecherl | `/static/about/team-andreas-hoecherl.svg` | **[PLACEHOLDER: SVG avatar with initials]** Replace with real headshot photo. |
| Team photo: Soeren Vorsmann | `/static/about/team-soeren-vorsmann.svg` | **[PLACEHOLDER: SVG avatar with initials]** Replace with real headshot photo. |
| Advisory photo: Rudi Siebel | `/static/about/team-rudi-siebel.svg` | **[PLACEHOLDER: SVG avatar with initials]** Replace with real headshot photo. |
| Advisory photo: Stephan Henkel | `/static/about/team-stephan-henkel.svg` | **[PLACEHOLDER: SVG avatar with initials]** Replace with real headshot photo. |
| Partner logo: Eskua AI | `/static/about/logo-eskua-ai.svg` | **[PLACEHOLDER: SVG text logo]** Replace with real company logo. |
| Partner logo: Keynum | `/static/about/logo-keynum.svg` | **[PLACEHOLDER: SVG text logo]** Replace with real company logo. |
| Partner logo: FACT First Cloud | `/static/about/logo-fact-first-cloud.svg` | **[PLACEHOLDER: SVG text logo]** Replace with real company logo. |
| Partner logo: Sopra Steria | `/static/about/logo-sopra-steria.svg` | **[PLACEHOLDER: SVG text logo]** Replace with real company logo. |

### 6.5 Tests to Update

| File | Changes |
|------|---------|
| `tests/component/components/pages/LandingPage.cy.ts` | Update to reflect new section order and removed/renamed components. |
| Any E2E tests referencing `data-test="join-campaign-button"` | Update selectors to match TheFrameworks. |
| Any E2E tests referencing YouTube/video embeds | Remove or update for new TheSocialProof section. |

### 6.6 Implementation Order (Recommended)

Execute in this order to minimize broken states:

1. **Create infrastructure:** `breakpoints.scss`, `useBreakpoint.ts`.
2. **Create new components (no wiring yet):** TheTrustBar, TheDataAccess, TheFrameworks, TheSocialProof, TheAboutPrinciples, TheAboutEcosystem, SuccessStoryPage.
3. **Update content data:** `content.json`, `aboutContent.ts`.
4. **Modify existing components:** TheIntro, TheStruggle, TheGetInTouch, TheAboutBottomCTA, PersonCard.
5. **Update page-level files:** LandingPage.vue, AboutPage.vue (swap imports/order). Add success stories route to router.
6. **Update navigation:** LandingPageHeader.vue (hamburger menu, skip link).
7. **Delete old components:** TheQuotes, TheHowItWorks, TheJoinCampaign, TheAboutSponsors, TheAboutPartners.
8. **Update tests.**
9. **Verify:** Run `npm run typecheck`, `npm run lint`, `npm run testcomponent`.

---

## Appendix: Quick Reference -- What Goes Where

### Landing Page Visual Flow

```
+------------------------------------------+
|  [Logo]    HOME  ABOUT    [Login][SignUp] |  <- LandingPageHeader (hamburger on mobile)
+------------------------------------------+
|                                          |
|         Liberate Data -                  |
|         Empower Autonomy.               |  <- TheIntro (Hero)
|     The alternative to data monopolies.  |
|     [Search for a company's ESG data]    |
|   [Create Free Account] [Get in Touch]   |
|                                          |
+------------------------------------------+
|  Dataland aims to fix four main issues   |
|  [Data Gaps] [Quality] [Usage] [Price]   |  <- TheStruggle (Problem)
+------------------------------------------+
|  Trusted by leaders in European finance  |
|  [BVI] [VOEB] [d-fine] [PwC] [...]      |  <- TheTrustBar (NEW)
+------------------------------------------+
|  How does Dataland work?                 |
|  [Search][Request][Data][Download]       |  <- TheDataAccess (was HowItWorks)
|  [Autoplay muted looping video]          |
+------------------------------------------+
|  Access data across key ESG frameworks   |
|  [EU Tax] [SFDR] [PCAF] [LkSG] [VSME]   |  <- TheFrameworks (was JoinCampaign)
|  [Create Free Account]                   |
+------------------------------------------+
|  What Our Members Say                    |
|  [Quote 1] [Quote 2]                    |  <- TheSocialProof (NEW)
|  [Quote 3] [Quote 4]                    |
|  [Success Story 1] [Story 2] [Story 3]  |  (summaries, link to /success-stories/:slug)
|  [Member Process Sketch]                 |
+------------------------------------------+
|  Already a trusted partner for top cos   |
|  [Logo] [Logo] [Logo] [Logo] ...        |  <- TheBrands (kept)
+------------------------------------------+
|  Get in touch                            |
|  [Photo] Dr. Moritz Kiese               |  <- TheGetInTouch (kept)
|         [Get in Touch]                   |
+------------------------------------------+
```

### About Page Visual Flow

```
+------------------------------------------+
|  Who Stands Behind Dataland              |
|  A non-profit ESG data platform...       |  <- TheAboutHero
|  [Get in Touch]                          |
+------------------------------------------+
|  Why Trust Dataland                      |
|  [Cannot be sold] [Institutionally...]   |  <- TheAboutTrustPillars
|  [Narrow scope]   [Transparent Tech]     |
+------------------------------------------+
|  Leadership Team                         |
|  [Moritz] [Andreas] [Soeren]             |  <- TheAboutTeam
+------------------------------------------+
|  Our Principles                          |
|  [Integrity]    [Disclosure] [Transp.]   |  <- TheAboutPrinciples (NEW)
|  [Accountab.]   [Neutrality] [Collab.]   |
+------------------------------------------+
|  Advisory Board                          |
|  [Rudi Siebel]  [Stephan Henkel]         |  <- TheAboutAdvisoryBoard
+------------------------------------------+
|  Our Ecosystem                           |
|  Sponsors: [T-Sys] [d-fine] [PwC] [E1]  |  <- TheAboutEcosystem (NEW)
|  Partners: [Eskua] [Keynum] [FACT] [SS]  |
+------------------------------------------+
|  Let Us Start the Conversation           |
|  [Talk to Our Team] [Learn More ...]     |  <- TheAboutBottomCTA
+------------------------------------------+
```
