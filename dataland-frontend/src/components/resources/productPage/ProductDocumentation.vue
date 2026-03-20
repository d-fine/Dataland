<template>
  <section id="documentation" class="product-docs" role="region" aria-labelledby="product-docs-heading">
    <div class="product-docs__wrapper">
      <h2 id="product-docs-heading" class="product-docs__heading">Documentation and tutorials</h2>
      <div class="product-docs__links">
        <a
          v-for="link in DOCUMENTATION_LINKS"
          :key="link.url"
          :href="link.url"
          target="_blank"
          rel="noopener noreferrer"
          class="product-docs__pill"
        >
          {{ link.label }}
          <i class="pi pi-external-link" aria-hidden="true" />
        </a>
      </div>
      <div class="product-docs__actions">
        <router-link to="/about#contact">
          <Button label="Get in touch" rounded severity="secondary" />
        </router-link>
        <Button label="Try it free" rounded @click="handleRegister" />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { registerAndRedirectToRedirectPage } from '@/utils/KeycloakUtils';
import { DOCUMENTATION_LINKS } from '@/components/resources/productPage/productContent';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const handleRegister = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      void registerAndRedirectToRedirectPage(keycloak);
    })
    .catch((error: unknown) => console.error(error));
};
</script>

<style scoped lang="scss">
.product-docs {
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

  &__links {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
  }

  &__pill {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border: 1px solid var(--p-surface-200, #e6e6e6);
    border-radius: 999px;
    font-size: 0.875rem;
    color: var(--p-text-color, #1b1b1b);
    text-decoration: none;

    &:hover {
      border-color: var(--p-primary-color);
      color: var(--p-primary-color);
    }

    i {
      font-size: 0.75rem;
    }
  }

  &__actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media only screen and (max-width: $bp-md) {
  .product-docs {
    padding: 40px 16px;

    &__heading {
      font-size: 1.5rem;
    }
  }
}
</style>
