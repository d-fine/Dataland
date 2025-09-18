<template>
  <section v-if="aboutIntroSection" class="about-intro" role="region" aria-label="About Intro Statement">
    <div class="about-intro__wrapper">
      <h2 id="about-intro-heading" aria-labelledby="about-intro-heading">
        {{ aboutIntroSection.text[0] }}
        <span>{{ aboutIntroSection.text[1] }}</span>
        {{ aboutIntroSection.text[2] }}
      </h2>
    </div>
    <Button :label="aboutIntroSection.title" @click="register" rounded />
  </section>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import type { Section } from '@/types/ContentTypes';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';
import { loginAndRedirectToRedirectPage, registerAndRedirectToSearchPage } from '@/utils/KeycloakUtils.ts';

const props = defineProps<{ sections?: Section[] }>();

const aboutIntroSection = computed(() => {
  return props.sections?.find((section) => section.title === 'START YOUR DATALAND JOURNEY') ?? null;
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

/**
 * Sends the user to the keycloak register page (if not authenticated already) and to the portfolio page else.
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        void registerAndRedirectToSearchPage(keycloak);
      } else {
        void loginAndRedirectToRedirectPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>

<style scoped lang="scss">
.about-intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 187px 0 243px;
  gap: 75px;

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 32px;
    width: 100%;
    padding: 0 32px;
  }

  h2,
  span {
    font-style: normal;
    font-weight: var(--font-weight-bold);
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;
    text-wrap: pretty;
    grid-column: 1 / -1;
    font-size: 64px;
    line-height: 72px;
    letter-spacing: 0.25px;
    margin: 0 auto;
    max-width: 900px;

    span {
      color: var(--p-primary-color);
    }
  }
}

@media only screen and (max-width: 1024px) {
  .about-intro {
    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
      padding: 0 22px;
      gap: 40px 22px;
    }
  }
}

@media only screen and (max-width: 768px) {
  .about-intro {
    padding: 32px 0 40px;
    background-color: var(--grey-tones-100);
    gap: 112px;

    button {
      display: none;
    }

    &__wrapper {
      gap: 56px 16px;
      padding: 0 16px;
    }

    h2 {
      grid-column: 1 / -1;
      font-size: 32px;
      line-height: 40px;
      text-align: left;
    }
  }
}
</style>
