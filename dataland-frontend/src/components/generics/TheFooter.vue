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
        <router-link to="/product#how-it-works" class="footer__link">How it works</router-link>
        <router-link to="/product#features" class="footer__link">Features</router-link>
        <router-link to="/product#frameworks" class="footer__link">Frameworks</router-link>
        <router-link to="/product#use-cases" class="footer__link">Use Cases</router-link>
        <router-link to="/product#customer-stories" class="footer__link">Customer Stories</router-link>
        <router-link to="/testimonials" class="footer__link">Testimonials</router-link>
        <router-link to="/product#membership-pricing" class="footer__link">Membership & Pricing</router-link>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-title">Company</h3>
        <router-link to="/#why-us" class="footer__link">Why Dataland</router-link>
        <router-link to="/about#company" class="footer__link">About us</router-link>
        <router-link to="/about#updates" class="footer__link">Updates and Insights</router-link>
        <router-link to="/about#partners" class="footer__link">Partners</router-link>
        <router-link to="/about#contact" class="footer__link">Contact</router-link>
      </div>
      <div class="footer__col">
        <h3 class="footer__col-title">Resources</h3>
        <router-link to="/product#documentation" class="footer__link">Tutorials</router-link>
        <router-link to="/product#documentation" class="footer__link">Platform documentation</router-link>
        <router-link to="/product#documentation" class="footer__link">Technical Hub</router-link>
      </div>
    </div>
    <div class="footer__bottom">
      <router-link to="/legal" class="footer__bottom-link">Legal</router-link>
      <router-link to="/imprint" class="footer__bottom-link">Imprint</router-link>
      <router-link to="/dataprivacy" class="footer__bottom-link">Data Privacy</router-link>
      <router-link to="/cookies" class="footer__bottom-link">Cookie Settings</router-link>
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
</script>

<style scoped lang="scss">
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

  &__col {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  &__col-title {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0 0 16px;
    color: var(--p-text-color, #1b1b1b);
  }

  &__description {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    line-height: 1.5;
    margin: 0 0 16px;
  }

  &__logos {
    display: flex;
    gap: 16px;
    align-items: center;
    flex-wrap: wrap;
  }

  &__sponsor-logo {
    height: 24px;
    object-fit: contain;
  }

  &__link {
    font-size: 0.875rem;
    color: var(--p-text-color, #1b1b1b);
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
    color: var(--p-text-muted-color, #737373);
    max-width: 1200px;
    margin-left: auto;
    margin-right: auto;
  }

  &__bottom-link {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    text-decoration: none;

    &:hover {
      color: var(--p-primary-color);
    }
  }

  &__copyright {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
  }

  &__linkedin {
    font-size: 1.125rem;
  }

  &--light {
    background: var(--p-surface-100, #f0f0f0);
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
</style>
