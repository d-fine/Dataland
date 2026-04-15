<template>
  <footer :class="['footer', { 'footer--light': isLightVersion }]" role="contentinfo" data-test="dataland footer">
    <div class="footer__top">
      <div class="footer__col">
        <h3 class="footer__col-title">Dataland</h3>
        <p class="footer__description">
          Non-profit sustainability data platform. Source-based. Human-verified. Open source.
        </p>
        <div class="footer__logos">
          <img src="/static/logos/logo_wertestiftung.png" alt="Werte-Stiftung logo" class="footer__sponsor-logo" />
          <img src="/static/logos/logo_dfine.svg" alt="d-fine logo" class="footer__sponsor-logo" />
          <img src="/static/logos/logo_pwc.svg" alt="PwC logo" class="footer__sponsor-logo" />
        </div>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-title">Product</h3>
        <a href="/product#how-it-works" class="footer__link">How it works</a>
        <a href="/product#features" class="footer__link">Features</a>
        <a href="/product#frameworks" class="footer__link">Frameworks</a>
        <a href="/product#use-cases" class="footer__link">Use Cases</a>
        <a href="/product#customer-stories" class="footer__link">Customer Stories</a>
        <a href="/testimonials" class="footer__link">Testimonials</a>
        <a href="/product#membership-pricing" class="footer__link">Membership & Pricing</a>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-title">Company</h3>
        <a href="/#why-us" class="footer__link">Why Dataland</a>
        <a href="/about#company" class="footer__link">About us</a>
        <a href="/about#updates" class="footer__link">Updates and Insights</a>
        <a href="/about#partners" class="footer__link">Partners</a>
        <a href="/about#contact" class="footer__link">Contact</a>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-title">Resources</h3>
        <a href="/product#documentation" class="footer__link">Tutorials</a>
        <a href="/product#documentation" class="footer__link">Platform documentation</a>
        <a href="/product#documentation" class="footer__link">Technical Hub</a>
      </div>
    </div>
    <div class="footer__bottom">
      <a href="/legal" class="footer__bottom-link">Legal</a>
      <a href="/imprint" class="footer__bottom-link">Imprint</a>
      <a href="/dataprivacy" class="footer__bottom-link">Data Privacy</a>
      <button type="button" class="footer__bottom-link footer__cookie-btn" @click="showCookieSettings">
        Cookie Settings
      </button>
      <span class="footer__copyright">&copy; {{ currentYear }} Dataland</span>
      <a
        href="https://www.linkedin.com/company/dataland-gmbh"
        target="_blank"
        rel="noopener noreferrer"
        class="footer__bottom-link footer__linkedin"
        aria-label="Follow Dataland on LinkedIn"
      >
        <i class="pi pi-linkedin" aria-hidden="true" />
      </a>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue';

defineProps<{
  isLightVersion?: boolean;
}>();

const currentYear = computed(() => new Date().getFullYear());

/** Opens the Cookiebot consent dialog if available. */
function showCookieSettings(): void {
  const win = window as Window & { Cookiebot?: { show: () => void } };
  if (typeof win.Cookiebot !== 'undefined') {
    win.Cookiebot.show();
  }
}
</script>

<style scoped>
.footer {
  background: var(--p-surface-0, #f7f7f5);
  border-top: 1px solid var(--p-surface-200, #e6e6e6);
  padding: 64px 64px 32px;
}

.footer__top {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.footer__col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.footer__col-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 16px;
  color: var(--p-text-color, #1b1b1b);
}

.footer__description {
  font-size: 0.875rem;
  color: var(--p-text-muted-color, #737373);
  line-height: 1.5;
  margin: 0 0 16px;
}

.footer__logos {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.footer__sponsor-logo {
  height: 24px;
  object-fit: contain;
}

.footer__link {
  font-size: 0.875rem;
  color: var(--p-text-color, #1b1b1b);
  text-decoration: none;
  display: block;
  padding: 4px 0;
}

.footer__link:hover {
  color: var(--p-primary-color);
}

.footer__bottom {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--p-surface-200, #e6e6e6);
  font-size: 0.875rem;
  color: var(--p-text-muted-color, #737373);
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.footer__bottom-link {
  font-size: 0.875rem;
  color: var(--p-text-muted-color, #737373);
  text-decoration: none;
}

.footer__bottom-link:hover {
  color: var(--p-primary-color);
}

.footer__cookie-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  font-family: inherit;
}

.footer__copyright {
  font-size: 0.875rem;
  color: var(--p-text-muted-color, #737373);
}

.footer__linkedin {
  font-size: 1.125rem;
}

.footer--light {
  background: var(--p-surface-100, #f0f0f0);
}

@media only screen and (max-width: 1024px) {
  .footer__top {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: 768px) {
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
</style>
