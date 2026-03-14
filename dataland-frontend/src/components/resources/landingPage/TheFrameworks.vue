<template>
  <section
    v-if="frameworksSection"
    id="frameworks"
    class="frameworks"
    role="region"
    aria-labelledby="frameworks-heading"
  >
    <div class="frameworks__wrapper">
      <h2 id="frameworks-heading" class="frameworks__title">
        {{ frameworksSection.text[0] }}
        <span v-if="frameworksSection.text[1]">{{ frameworksSection.text[1] }}</span
        >{{ frameworksSection.text[2] }}
      </h2>
      <p v-if="frameworksSection.text[3]" class="frameworks__subtitle">
        {{ frameworksSection.text[3] }}
      </p>
      <Button
        label="Create Free Account"
        data-test="frameworks-register-button"
        aria-label="Create a free Dataland account"
        rounded
        @click="handleRegister"
        :pt="{
          root: {
            style: {
              width: 'fit-content',
              whiteSpace: 'nowrap',
            },
          },
        }"
      />
      <div class="frameworks__grid" role="list" aria-label="Supported ESG frameworks">
        <div v-for="(card, index) in frameworksSection.cards" :key="index" class="frameworks__card" role="listitem">
          <div class="frameworks__card-title">
            <span>{{ card.title }}</span>
          </div>
          <div class="frameworks__card-text">
            {{ card.text }}
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, inject } from 'vue';
import type { Section } from '@/types/ContentTypes';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { registerAndRedirectToRedirectPage } from '@/utils/KeycloakUtils';
import Button from 'primevue/button';

const props = defineProps<{ sections?: Section[] }>();

const frameworksSection = computed(() => {
  return props.sections?.find((section) => section.title === 'Frameworks') ?? null;
});

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
.frameworks {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 140px 0;

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 61px 32px;
    max-width: 900px;
    padding: 0 32px;
  }

  &__title,
  &__title * {
    font-size: 64px;
    font-style: normal;
    font-weight: 700;
    line-height: 72px;
    letter-spacing: 0.25px;
    text-align: left;
    margin: 0;
    grid-column: 1 / -1;
    transition:
      font-size 0.4s ease,
      line-height 0.4s ease;

    span {
      color: var(--p-primary-color);
    }
  }

  &__subtitle {
    font-size: 20px;
    font-style: normal;
    font-weight: 400;
    line-height: 30px;
    letter-spacing: 0.25px;
    grid-column: 1 / -1;
    text-align: left;
    margin: 0;
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    align-self: flex-end;
    gap: 32px;
    grid-column: 1 / -1;
    margin-top: 40px;
    justify-content: center;
  }

  &__card {
    border-radius: 16px;
    border: 1px solid rgba(244, 244, 244, 0.33);
    background: var(--basic-white, #fff);
    box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.08);
    display: flex;
    gap: 16px;
    flex-direction: column;
    padding: 46px 32px;
    text-align: left;
    min-width: 228px;
  }

  &__card-title,
  &__card-title * {
    color: var(--p-highlight-color);
    font-size: 32px;
    font-style: normal;
    font-weight: 600;
    line-height: 40px;
    letter-spacing: 0.25px;
  }

  &__card-text {
    color: var(--grey-tones-900);
    font-size: 16px;
    font-style: normal;
    font-weight: 400;
    line-height: 24px;
    letter-spacing: 0.25px;
  }
}

@media only screen and (max-width: $bp-lg) {
  .frameworks {
    padding: 80px 0;

    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
    }

    &__title {
      font-size: 48px;
      line-height: 56px;
      max-width: 551px;
    }

    &__grid {
      grid-template-columns: repeat(2, 1fr);
      gap: 61px 22px;
      grid-column: 1 / -1;
      margin-top: 0;
    }

    &__card {
      gap: 24px;
      padding: 46px 40px;
      min-height: 340px;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .frameworks {
    padding: 64px 0;

    &__wrapper {
      gap: 32px 16px;
      padding: 0 16px;
    }

    &__title {
      font-size: 40px;
      line-height: 48px;
    }

    &__subtitle {
      font-size: 18px;
      line-height: 28px;
    }

    &__grid {
      grid-template-columns: 1fr;
      gap: 32px;
      grid-column: 1 / -1;
    }

    &__card {
      min-height: unset;
    }
  }
}

@media only screen and (max-width: $bp-sm) {
  .frameworks {
    &__title {
      font-size: 32px;
      line-height: 40px;
    }

    &__subtitle {
      font-size: 18px;
      line-height: 28px;
    }
  }
}
</style>
