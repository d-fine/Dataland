<template>
  <section v-if="aboutIntroSection" class="about-intro" role="region" aria-label="About Intro Statement">
    <div class="about-intro__wrap-backlink">
      <router-link to="/" class="about-intro__backlink">
        {{ aboutIntroSection.text[3] }}
      </router-link>
    </div>
    <div class="about-intro__wrapper">
      <h2 id="about-intro-heading" aria-labelledby="about-intro-heading">
        {{ aboutIntroSection.text[0] }}
        <span>{{ aboutIntroSection.text[1] }}</span>
        {{ aboutIntroSection.text[2] }}
      </h2>
    </div>
    <ButtonComponent
      :label="aboutIntroSection.title"
      buttonType="button-component quotes__button"
      :aria-label="aboutIntroSection.title"
      @click="register"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import type { Section } from '@/types/ContentTypes';
import ButtonComponent from '@/components/resources/newLandingPage/ButtonComponent.vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { registerAndRedirectToSearchPage } from '@/utils/KeycloakUtils';
import type Keycloak from 'keycloak-js';
const props = defineProps<{ sections?: Section[] }>();

const aboutIntroSection = computed(() => {
  return props.sections?.find((section) => section.title === 'START YOUR DATALAND JOURNEY') ?? null;
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
/**
 * Sends the user to the keycloak register page (if not authenticated already)
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        void registerAndRedirectToSearchPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables';

.about-intro {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 187px 0 243px;
  gap: 75px;
  &__backlink {
    display: none;
  }
  &__wrapper {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 32px;
    width: 100%;
    padding: 0 32px;
  }
  h2 {
    font-style: normal;
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
      color: var(--primary-orange);
    }
  }
}

@media only screen and (max-width: newVariables.$medium) {
  .about-intro {
    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
      padding: 0 22px;
      gap: 40px 22px;
    }
  }
}

@media only screen and (max-width: newVariables.$small) {
  .about-intro {
    padding: 32px 0 40px;
    background-color: var(--grey-tones-100);
    gap: 112px;

    button {
      display: none;
    }
    &__wrap-backlink {
      background-color: white;
      width: 100%;
      padding: 2em;
      text-align: left;
      margin: -2em auto;
    }
    &__backlink {
      color: var(--basic-dark);
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px; /* 150% */
      letter-spacing: 0.75px;
      text-transform: uppercase;
      display: block;
      cursor: pointer;
      text-decoration: none;
      position: relative;
      border-bottom: 2px solid transparent;
      width: fit-content;
      &::before {
        content: '';
        position: absolute;
        transform: translateY(-50%) rotate(180deg);
        left: -20px;
        top: 50%;
        width: 16px;
        height: 16px;
        background-image: url('/static/icons/Arrow--right.svg');
        background-size: cover;
      }
      &:hover {
        border-bottom: 2px solid var(--primary-orange);
        color: var(--primary-orange);
        &::before {
          filter: invert(44%) sepia(83%) saturate(1846%) hue-rotate(351deg) brightness(101%) contrast(101%);
        }
      }
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
