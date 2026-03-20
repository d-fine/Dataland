# Homepage Rework -- Backlog (Backend-Dependent Features)

**Date:** 2026-03-18
**Status:** DEFERRED -- Requires backend integration
**Related spec:** `HOMEPAGE_REWORK_SPEC.md`

---

## Purpose

This file documents features from Valeria's `DL_Homepage_Spec` that are excluded from the main specification because they require backend service integration. The main spec covers static frontend changes only.

These features have static layout placeholders in the main spec. Once the corresponding backend services are available, the features listed here can be implemented by wiring the existing UI to the backend.

---

## 1. Contact / Demo Request Form Submission

**Pages affected:** About page (`/about#contact`), Contact page (`/contact`)

**Current state in main spec:** The "Request a demo" form renders with fields (Name, Email, Message, privacy consent, submit button) but does not submit data.

**Backend requirements:**

- Email service endpoint to receive form submissions
- SMTP or equivalent email delivery integration
- Privacy consent logging (GDPR)
- Rate limiting / spam protection (CAPTCHA or honeypot)
- Success/error feedback states in the UI

**Form fields:**

| Field | Type | Label | Placeholder | Required |
|-------|------|-------|-------------|----------|
| name | text | Name | Full name | Yes |
| email | email | Email address | name@company.com | Yes |
| message | textarea | Message | Any relevant information | No |
| consent | checkbox | "By clicking here you agree to the privacy policy" (linked to `/dataprivacy`) | — | Yes |

**Submit action:** POST to email service endpoint. On success: show confirmation message. On error: show error message with retry option.

---

## 2. Newsletter Subscription

**Pages affected:** Newsletter page (`/newsletter`), Landing page (CTA buttons), About page (CTA buttons)

**Current state in main spec:** The Newsletter page renders a signup form layout but does not submit data. CTA buttons across the site link to `/newsletter`.

**Backend requirements:**

- Newsletter subscription endpoint (email service or third-party like Mailchimp/Brevo)
- Double opt-in flow (confirmation email)
- Unsubscribe mechanism
- GDPR consent logging
- Rate limiting

**Form fields:**

| Field | Type | Label | Placeholder | Required |
|-------|------|-------|-------------|----------|
| name | text | Name | Full name | Yes |
| email | email | Email address | name@company.com | Yes |
| organisation | text | Organisation | Your company or institution | No |
| consent | checkbox | "By signing up you agree to the privacy policy" (linked to `/dataprivacy`) | — | Yes |

**Submit action:** POST to newsletter service. Trigger double opt-in confirmation email.

---

## 3. Video Testimonials (YouTube Embed)

**Pages affected:** Testimonials page (`/testimonials`)

**Current state in main spec:** The Testimonials page shows text-only quote cards with a visual placeholder for video. No video embeds.

**Backend/integration requirements:**

- YouTube IFrame API integration
- Cookie consent handling (GDPR — YouTube sets tracking cookies)
- Integration with existing cookie consent banner
- Lazy-loading of YouTube iframes (performance)
- Fallback for users who decline cookies (show static thumbnail + link to YouTube)

**Video testimonial data:**

Each testimonial in the data file (`landingContent.ts`) has a `video_url` field that is currently empty. When video embed is implemented:

1. Add YouTube video IDs to the testimonial data
2. Render YouTube iframe only after cookie consent for "Marketing" cookies
3. Before consent: show a static thumbnail with a "Watch on YouTube" link
4. After consent: lazy-load iframe with `IntersectionObserver`

**Existing reference:** The old `TheQuotes.vue` component had YouTube embed logic that was removed. It can serve as a reference for the cookie consent integration pattern.

---

## 4. Platform Demo Video

**Pages affected:** Product page (potential future addition)

**Current state:** Not included in main spec.

**Description:** Self-hosted short video (few seconds, silent, looping) showing platform navigation impressions. Was included in the existing `LANDING_ABOUT_REWORK_SPEC.md` as part of `TheDataAccess` section.

**Requirements:**

- Create/record a short screen capture of the platform
- Host the mp4 file (`/static/videos/platform-demo.mp4`)
- Create a poster image (`/static/videos/platform-demo-thumb.svg` or `.webp`)
- Implement lazy-loading with `IntersectionObserver`
- Autoplay muted loop with `playsinline`

---

## 5. Cookie Settings Page

**Route:** `/cookies`

**Current state:** Route exists in Valeria's spec but the page is not detailed.

**Requirements:**

- Integration with cookie consent management solution
- UI for toggling cookie categories (Necessary, Analytics, Marketing)
- Persistence of preferences
- GDPR compliance documentation

---

## Implementation Priority (Suggested)

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| 1 | Contact form submission | Medium | High — enables lead capture |
| 2 | Newsletter subscription | Medium | Medium — enables audience building |
| 3 | Video testimonials | Low-Medium | Medium — social proof enhancement |
| 4 | Platform demo video | Low | Low — visual enhancement |
| 5 | Cookie settings page | Medium | Compliance requirement |
