<template>
  <section id="membership-pricing" class="product-pricing" role="region" aria-labelledby="product-pricing-heading">
    <div class="product-pricing__wrapper">
      <h2 id="product-pricing-heading" class="product-pricing__heading">Membership and pricing</h2>
      <p class="product-pricing__subheading">Full data access. Shared cost for new data procurement.</p>

      <div class="product-pricing__value-props">
        <div v-for="(prop, index) in VALUE_PROPOSITIONS" :key="index" class="value-prop">
          <!-- [PLACEHOLDER] icons to be provided by design team -->
          <img :src="prop.icon" :alt="prop.title" class="value-prop__icon" />
          <h3 class="value-prop__title">{{ prop.title }}</h3>
          <p class="value-prop__text">{{ prop.text }}</p>
        </div>
      </div>

      <div class="product-pricing__details">
        <div class="pricing-card">
          <h3 class="pricing-card__title">{{ PRICING_CARD.title }}</h3>
          <ul class="pricing-card__list">
            <li v-for="item in PRICING_CARD.items" :key="item" class="pricing-card__item">{{ item }}</li>
          </ul>
          <p class="pricing-card__footer">{{ PRICING_CARD.footer }}</p>
        </div>
        <div class="credits-visual">
          <h3 class="credits-visual__title">{{ CREDITS_VISUAL.title }}</h3>
          <!-- [PLACEHOLDER] credits visual diagram to be provided by design team -->
          <img :src="CREDITS_VISUAL.image" :alt="CREDITS_VISUAL.caption" class="credits-visual__image" />
          <p class="credits-visual__caption">{{ CREDITS_VISUAL.caption }}</p>
        </div>
      </div>

      <p class="product-pricing__bottom-note">{{ PRICING_BOTTOM_NOTE }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import {
  VALUE_PROPOSITIONS,
  PRICING_CARD,
  CREDITS_VISUAL,
  PRICING_BOTTOM_NOTE,
} from '@/components/resources/productPage/productContent';
</script>

<style scoped lang="scss">
.product-pricing {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 40px;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__subheading {
    font-size: 1.125rem;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
    text-align: center;
  }

  &__value-props {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    width: 100%;
  }

  &__details {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 32px;
    width: 100%;
  }

  &__bottom-note {
    font-size: 1rem;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
    text-align: center;
    max-width: 800px;
    line-height: 1.5;
  }
}

.value-prop {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;

  &__icon {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }

  &__title {
    font-size: 1.125rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__text {
    font-size: 0.875rem;
    line-height: 1.5;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
  }
}

.pricing-card {
  background: var(--p-surface-50, #f7f7f5);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 12px;
  padding: 32px;

  &__title {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0 0 16px;
    color: var(--p-text-color, #1b1b1b);
  }

  &__list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__item {
    font-size: 1rem;
    line-height: 1.5;
    color: var(--p-text-color, #1b1b1b);
    padding-left: 24px;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 8px;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--p-primary-color);
    }
  }

  &__footer {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    margin: 16px 0 0;
    font-style: italic;
  }
}

.credits-visual {
  display: flex;
  flex-direction: column;
  gap: 16px;

  &__title {
    font-size: 1.25rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__image {
    width: 100%;
    height: auto;
    border-radius: 8px;
  }

  &__caption {
    font-size: 0.875rem;
    color: var(--p-text-muted-color, #737373);
    margin: 0;
    font-style: italic;
  }
}

@media only screen and (max-width: $bp-lg) {
  .product-pricing__value-props {
    grid-template-columns: 1fr;
    max-width: 500px;
    margin: 0 auto;
  }
}

@media only screen and (max-width: $bp-md) {
  .product-pricing {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }

    &__details {
      grid-template-columns: 1fr;
    }
  }
}
</style>
