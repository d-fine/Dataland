<template>
  <section class="frameworks" role="region" aria-labelledby="frameworks-heading">
    <div class="frameworks__wrapper">
      <h2 id="frameworks-heading" class="frameworks__heading">Supported regulatory frameworks</h2>
      <p class="frameworks__subheading">
        Structured data for the frameworks that drive European sustainability reporting.
      </p>
      <div class="frameworks__grid">
        <router-link
          v-for="(card, index) in FRAMEWORK_CARDS"
          :key="index"
          to="/product#frameworks"
          class="framework-card"
        >
          <h3 class="framework-card__title">{{ card.title }}</h3>
          <p v-if="card.subtitle" class="framework-card__subtitle">{{ card.subtitle }}</p>
          <p class="framework-card__text">{{ card.description }}</p>
        </router-link>
      </div>
      <p class="frameworks__bottom-text">
        New frameworks, datasets, and features are added based on member input and regulatory developments. Become a
        member to help shape what comes next.
      </p>
      <div class="frameworks__actions">
        <router-link to="/about#updates">
          <Button label="News & insights" rounded severity="secondary" />
        </router-link>
        <router-link to="/newsletter">
          <Button label="Subscribe to our Newsletter" rounded severity="secondary" />
        </router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { FRAMEWORK_CARDS } from '@/components/resources/landingPage/landingContent';
</script>

<style scoped lang="scss">
.frameworks {
  padding: 80px 64px;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 32px;
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
    max-width: 700px;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    width: 100%;
  }

  &__bottom-text {
    font-size: 1rem;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
    text-align: center;
    max-width: 700px;
    line-height: 1.5;
  }

  &__actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

.framework-card {
  background: var(--p-surface-0, #ffffff);
  border: 1px solid var(--p-surface-200, #e6e6e6);
  border-radius: 8px;
  padding: 24px;
  position: relative;
  overflow: hidden;
  text-decoration: none;
  display: block;

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
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__subtitle {
    font-size: 1.25rem;
    color: var(--p-text-muted-color, #737373);
    margin: 4px 0 0;
  }

  &__text {
    font-size: 0.875rem;
    line-height: 1.5;
    margin: 12px 0 0;
    color: var(--p-text-muted-color, #737373);
  }
}

@media only screen and (max-width: $bp-lg) {
  .frameworks__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media only screen and (max-width: $bp-md) {
  .frameworks {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }

  .frameworks__grid {
    grid-template-columns: 1fr;
  }
}
</style>
