<template>
  <main role="main" v-if="story">
    <section class="story-hero" role="region" aria-labelledby="story-hero-heading">
      <div class="story-hero__wrapper">
        <div class="story-hero__tags">
          <span class="story-hero__tag story-hero__tag--company">{{ story.companyType }}</span>
          <span class="story-hero__tag story-hero__tag--framework">{{ story.framework }}</span>
        </div>
        <h1 id="story-hero-heading" class="story-hero__headline">{{ story.title }}</h1>
        <p class="story-hero__summary">{{ story.summary }}</p>
      </div>
    </section>

    <section class="story-section story-section--challenge" role="region" aria-labelledby="story-challenge-heading">
      <div class="story-section__wrapper">
        <h2 id="story-challenge-heading" class="story-section__heading">The Challenge</h2>
        <p class="story-section__body">{{ story.challenge }}</p>
      </div>
    </section>

    <section class="story-section story-section--process" role="region" aria-labelledby="story-process-heading">
      <div class="story-section__wrapper">
        <h2 id="story-process-heading" class="story-section__heading">The Process</h2>
        <p class="story-section__body">{{ story.process }}</p>
        <img
          v-if="story.processSketchPath"
          :src="story.processSketchPath"
          :alt="`Process diagram for ${story.title}`"
          class="story-section__sketch"
        />
      </div>
    </section>

    <section class="story-section story-section--result" role="region" aria-labelledby="story-result-heading">
      <div class="story-section__wrapper">
        <h2 id="story-result-heading" class="story-section__heading">The Result</h2>
        <p class="story-section__body">{{ story.result }}</p>
      </div>
    </section>

    <section
      v-if="story.quote"
      class="story-quote"
      role="region"
      aria-labelledby="story-quote-heading"
    >
      <div class="story-quote__wrapper">
        <h2 id="story-quote-heading" class="visually-hidden">Quote</h2>
        <blockquote class="story-quote__blockquote">
          <p class="story-quote__text">"{{ story.quote.text }}"</p>
          <footer class="story-quote__attribution">
            <cite class="story-quote__cite">
              <span class="story-quote__name">{{ story.quote.attribution }}</span>
              <span class="story-quote__role">{{ story.quote.role }}</span>
            </cite>
          </footer>
        </blockquote>
      </div>
    </section>

    <section class="story-cta" role="region" aria-labelledby="story-cta-heading">
      <div class="story-cta__wrapper">
        <h2 id="story-cta-heading" class="story-cta__headline">Ready to Solve Your Data Challenges?</h2>
        <div class="story-cta__actions">
          <Button
            label="Start Using Dataland"
            rounded
            data-test="story-cta-register"
            aria-label="Register for Dataland"
            @click="handleRegister"
          />
          <router-link
            :to="{ path: '/', hash: '#social-proof' }"
            class="story-cta__link"
            data-test="story-cta-more-stories"
          >
            <Button
              label="Read More Stories"
              rounded
              severity="secondary"
              aria-label="Read more customer success stories"
            />
          </router-link>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Button from 'primevue/button';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { SUCCESS_STORIES } from '@/components/resources/successStories/successStoryContent';

const route = useRoute();
const router = useRouter();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const slug = route.params.slug as string;
const story = SUCCESS_STORIES.find((s) => s.slug === slug);

if (!story) {
  void router.replace('/');
}

const handleRegister = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) {
        void router.push({ path: '/platform-redirect' });
      } else {
        keycloak.register().catch((error: unknown) => console.error(error));
      }
    })
    .catch((error: unknown) => console.error(error));
};
</script>

<style scoped lang="scss">
main {
  margin-top: 132px;

  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}

.story-hero {
  padding: 4rem 2rem 3rem;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  &__tags {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
  }

  &__tag {
    display: inline-block;
    padding: 0.25rem 0.75rem;
    border-radius: 999px;
    font-size: 0.875rem;
    font-weight: 600;
    line-height: 1.5;

    &--company {
      background: var(--p-surface-100, #f0f0f0);
      color: var(--p-text-color, #1b1b1b);
    }

    &--framework {
      background: var(--p-primary-color, #ff6813);
      color: var(--default-neutral-white, #ffffff);
    }
  }

  &__headline {
    font-size: 2.5rem;
    font-weight: 700;
    line-height: 1.2;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__summary {
    font-size: 1.125rem;
    font-weight: 400;
    line-height: 1.7;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
  }
}

.story-section {
  padding: 3rem 2rem;
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &--process {
    background: var(--p-surface-50, #fafafa);
  }

  &__wrapper {
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  &__heading {
    font-size: 1.75rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__body {
    font-size: 1rem;
    font-weight: 400;
    line-height: 1.7;
    margin: 0;
    color: var(--p-text-muted-color, #585858);
  }

  &__sketch {
    width: 100%;
    max-width: 700px;
    height: auto;
    margin: 1rem auto 0;
    display: block;
    border-radius: 8px;
    border: 1px solid var(--p-surface-200, #dadada);
  }
}

.story-quote {
  padding: 4rem 2rem;
  background: var(--p-surface-0, #ffffff);
  border-bottom: 1px solid var(--p-surface-200, #dadada);

  &__wrapper {
    max-width: 800px;
    margin: 0 auto;
  }

  &__blockquote {
    margin: 0;
    padding: 0 0 0 1.5rem;
    border-left: 4px solid var(--p-primary-color, #ff6813);
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
  }

  &__text {
    font-size: 1.25rem;
    font-weight: 500;
    font-style: italic;
    line-height: 1.6;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__attribution {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }

  &__cite {
    font-style: normal;
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }

  &__name {
    font-size: 1rem;
    font-weight: 600;
    color: var(--p-text-color, #1b1b1b);
  }

  &__role {
    font-size: 0.875rem;
    font-weight: 400;
    color: var(--p-text-muted-color, #585858);
  }
}

.story-cta {
  padding: 5rem 2rem;
  background: var(--p-surface-0, #ffffff);
  text-align: center;

  &__wrapper {
    max-width: 600px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2rem;
  }

  &__headline {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
  }

  &__actions {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
    justify-content: center;
  }

  &__link {
    text-decoration: none;
  }
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media only screen and (max-width: $bp-lg) {
  .story-hero {
    &__headline {
      font-size: 2rem;
    }
  }
}

@media only screen and (max-width: $bp-md) {
  .story-hero {
    padding: 2.5rem 1rem 2rem;

    &__headline {
      font-size: 1.75rem;
    }

    &__summary {
      font-size: 1rem;
    }
  }

  .story-section {
    padding: 2rem 1rem;
  }

  .story-quote {
    padding: 2.5rem 1rem;

    &__text {
      font-size: 1.125rem;
    }
  }

  .story-cta {
    padding: 3rem 1rem;

    &__actions {
      flex-direction: column;
      width: 100%;
    }

    &__link {
      width: 100%;
    }
  }
}
</style>
