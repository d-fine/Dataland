<template>
  <section class="intro" role="region" aria-label="Introduction">
    <div class="intro__content">
      <h1 class="intro__headline">
        Democratizing access to high-quality sustainability data:<br />
        <span class="intro__headline--highlight">A European non-profit shared data platform</span>
      </h1>
      <p class="intro__subtext">
        Source-based data. Human-verified quality. On-demand sourcing. Structured for regulatory reporting.
      </p>
      <div class="intro__actions">
        <Button label="Try it free" rounded data-test="hero-register-button" @click="handleRegister" />
        <Button label="Get in touch" rounded severity="secondary" @click="handleContact" />
      </div>
    </div>
    <div class="intro__illustration">
      <!-- [PLACEHOLDER] intro_art.svg asset to be provided by design team -->
      <img :src="'/static/images/intro_art.svg'" alt="Dataland platform illustration" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';
import { useRouter } from 'vue-router';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { registerAndRedirectToRedirectPage } from '@/utils/KeycloakUtils';

const router = useRouter();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const handleRegister = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      void registerAndRedirectToRedirectPage(keycloak);
    })
    .catch((error: unknown) => console.error(error));
};

const handleContact = (): void => {
  void router.push('/about#contact');
};
</script>

<style scoped lang="scss">
.intro {
  display: grid;
  grid-template-columns: 60% 40%;
  align-items: center;
  min-height: 560px;
  padding: 80px 64px;
  max-width: 1440px;
  margin: 0 auto;

  &__content {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  &__headline {
    font-size: 48px;
    font-weight: 700;
    line-height: 1.15;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);

    &--highlight {
      color: var(--p-primary-color);
    }
  }

  &__subtext {
    font-size: 20px;
    line-height: 1.5;
    margin: 0;
    color: var(--p-text-muted-color, #737373);
  }

  &__actions {
    display: flex;
    gap: 16px;
    margin-top: 8px;
  }

  &__illustration {
    display: flex;
    justify-content: center;
    align-items: center;

    img {
      max-width: 100%;
      height: auto;
    }
  }
}

@media only screen and (max-width: $bp-xl) {
  .intro {
    &__headline {
      font-size: 40px;
    }
  }
}

@media only screen and (max-width: $bp-lg) {
  .intro {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 64px;

    &__headline {
      font-size: 36px;
    }

    &__illustration {
      order: 1;
    }

    &__content {
      order: 0;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .intro {
    padding: 40px 16px;
    min-height: auto;

    &__headline {
      font-size: 32px;
    }

    &__subtext {
      font-size: 18px;
    }

    &__actions {
      flex-direction: column;
      align-items: stretch;
    }
  }
}
</style>
