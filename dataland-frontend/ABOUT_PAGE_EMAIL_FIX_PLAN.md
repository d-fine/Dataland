# About Page Email Buttons — Bug Fix Plan

**Date:** 2026-03-13
**Branch:** `feature/rework-about-page` (existing, do NOT create a new branch)

---

## Problem

The "Get in Touch" button (TheAboutHero.vue) and the "Talk to Our Team" button (TheAboutBottomCTA.vue) on the About page use a hardcoded `window.location.href = 'mailto:...'` with only a subject line and no email body. This makes them effectively useless — they open a blank email that users won't send.

The Home page "GET IN TOUCH" button (TheGetInTouch.vue) already works correctly via the `openEmailClient()` utility, but its pre-filled email body (in content.json) is too long and reads like a form letter.

---

## Solution

### Phase 1: Update email content

**File: `src/assets/content.json`**

1. **Update card index 3** in the "Get in touch" section (the card used by TheGetInTouch.vue on the Home page):
   - Subject: `Dataland - Inquiry about Membership and Data Access`
   - Body:

     ```
     Dear Dr. Kiese,

     I came across Dataland and am interested in learning how the platform could help us procure structured sustainability data more efficiently.

     Could we arrange a short call to discuss our requirements and how Dataland's member model works in practice?

     Kind regards,
     [Your Name]
     [Your Organisation]
     ```

2. **Add a new card (index 4)** in the same "Get in touch" section for the About page buttons:
   - icon: `moritz.kiese@dataland.com`
   - title (subject): `Dataland - Interest in Collaboration`
   - text (body):

     ```
     Dear Dr. Kiese,

     I have read about Dataland's mission and governance model and would like to explore how a membership could support our sustainability data workflows.

     I would welcome the opportunity to schedule a brief conversation at your convenience.

     Kind regards,
     [Your Name]
     [Your Organisation]
     ```

### Phase 2: Wire up About page buttons

**File: `src/components/resources/aboutPage/TheAboutHero.vue`**

- Import `openEmailClient` from `@/utils/Email`
- Import `content` from `@/assets/content.json`
- Look up the "Get in touch" section and extract card index 4 (the About page email card)
- Replace the hardcoded `window.location.href` with a call to `openEmailClient(card)`

**File: `src/components/resources/aboutPage/TheAboutBottomCTA.vue`**

- Same changes as TheAboutHero.vue — import utility, look up card, replace hardcoded mailto

### Phase 3: Validation

- Run `npm run typecheck` — must pass
- Run `npm run lint` — must pass
- Manual verification: both About page buttons and the Home page button should open the email client with the correct pre-filled subject and body

---

## Agent Roles

| Phase | Agent                           | Task                                                       |
| ----- | ------------------------------- | ---------------------------------------------------------- |
| 1-2   | `frontend-developer` (Builder)  | Implement all code changes                                 |
| 3     | `frontend-developer` (Reviewer) | Independent code review — fresh agent, never saw the build |

---

## Files Touched

| File                                                       | Change                                              |
| ---------------------------------------------------------- | --------------------------------------------------- |
| `src/assets/content.json`                                  | Update card 3 body text, add card 4 for about page  |
| `src/components/resources/aboutPage/TheAboutHero.vue`      | Use `openEmailClient()` instead of hardcoded mailto |
| `src/components/resources/aboutPage/TheAboutBottomCTA.vue` | Use `openEmailClient()` instead of hardcoded mailto |
