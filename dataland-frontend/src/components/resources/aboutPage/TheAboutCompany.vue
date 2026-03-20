<template>
  <section id="company" class="about-company" role="region" aria-labelledby="about-company-heading">
    <div class="about-company__wrapper">
      <div class="about-company__logos">
        <img
          :src="'/static/logos/gfx_logo_dataland_orange_S.svg'"
          alt="Dataland logo"
          class="about-company__logo about-company__logo--large"
        />
        <img
          src="/static/logos/logo_wertestiftung.png"
          alt="Werte-Stiftung logo"
          class="about-company__logo about-company__logo--medium"
        />
        <div class="about-company__logo-row">
          <img
            :src="'/static/logos/logo_dfine.svg'"
            alt="d-fine logo"
            class="about-company__logo about-company__logo--small"
          />
          <img
            :src="'/static/logos/logo_pwc.svg'"
            alt="PwC logo"
            class="about-company__logo about-company__logo--small"
          />
        </div>
      </div>
      <div class="about-company__text">
        <h2 id="about-company-heading" class="about-company__title">{{ COMPANY_COPY.title }}</h2>
        <p class="about-company__paragraph">{{ COMPANY_COPY.text1 }}</p>
        <p class="about-company__paragraph">{{ COMPANY_COPY.text2 }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { COMPANY_COPY } from '@/components/resources/aboutPage/aboutContent';
</script>

<style scoped lang="scss">
.about-company {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 40% 60%;
    gap: 48px;
    align-items: center;
  }

  &__logos {
    display: flex;
    flex-direction: column;
    gap: 24px;
    align-items: center;
  }

  &__logo {
    object-fit: contain;

    &--large {
      height: 56px;
    }

    &--medium {
      height: 48px;
    }

    &--small {
      height: 42px;
    }
  }

  &__logo-row {
    display: flex;
    gap: 32px;
    align-items: center;
  }

  &__text {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  &__title {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__paragraph {
    font-size: 1rem;
    line-height: 1.6;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
  }
}

@media only screen and (max-width: $bp-md) {
  .about-company {
    padding: 40px 16px;

    &__wrapper {
      grid-template-columns: 1fr;
      gap: 32px;
    }

    &__title {
      font-size: 1.5rem;
    }
  }
}
</style>
