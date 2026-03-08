<template>
  <section class="social-proof" role="region" aria-labelledby="social-proof-heading" data-test="social-proof">
    <div class="social-proof__wrapper">
      <h2 id="social-proof-heading" class="social-proof__heading">What Our Members Say</h2>

      <!-- Sub-section A: Quote cards -->
      <div v-if="isMobileView" class="social-proof__quotes-mobile">
        <SlideShow
          slides-wrapper-classes="social-proof__slider-wrapper"
          slides-container-classes="social-proof__slider-container"
          arrows-container-classes="social-proof__slider-arrows"
          left-arrow-classes="social-proof__slider-arrow social-proof__slider-arrow--left"
          right-arrow-classes="social-proof__slider-arrow social-proof__slider-arrow--right"
          :slide-count="SOCIAL_PROOF_QUOTES.length"
          :slide-width="quoteSlideWidth"
        >
          <div
            v-for="(quote, index) in SOCIAL_PROOF_QUOTES"
            :key="index"
            class="social-proof__quote-slide"
            data-test="social-proof-quote"
          >
            <div class="social-proof__quote-card">
              <span class="social-proof__quote-mark" aria-hidden="true">&ldquo;</span>
              <blockquote class="social-proof__quote-text">{{ quote.text }}</blockquote>
              <div class="social-proof__quote-attribution">
                <span class="social-proof__quote-name">{{ quote.name }}</span>
                <span class="social-proof__quote-role">{{ quote.role }}</span>
              </div>
            </div>
          </div>
        </SlideShow>
      </div>
      <div v-else class="social-proof__quotes-grid">
        <div
          v-for="(quote, index) in SOCIAL_PROOF_QUOTES"
          :key="index"
          class="social-proof__quote-card"
          data-test="social-proof-quote"
        >
          <span class="social-proof__quote-mark" aria-hidden="true">&ldquo;</span>
          <blockquote class="social-proof__quote-text">{{ quote.text }}</blockquote>
          <div class="social-proof__quote-attribution">
            <span class="social-proof__quote-name">{{ quote.name }}</span>
            <span class="social-proof__quote-role">{{ quote.role }}</span>
          </div>
        </div>
      </div>

      <!-- Sub-section B: Success story summary cards -->
      <div class="social-proof__stories-grid">
        <div
          v-for="story in SUCCESS_STORY_SUMMARIES"
          :key="story.slug"
          class="social-proof__story-card"
          data-test="social-proof-story"
        >
          <h3 class="social-proof__story-title">{{ story.title }}</h3>
          <p class="social-proof__story-summary">{{ story.summary }}</p>
          <router-link
            :to="`/success-stories/${story.slug}`"
            class="social-proof__story-link"
            data-test="social-proof-story-link"
          >
            Read full story
            <i class="pi pi-arrow-right" aria-hidden="true" />
          </router-link>
        </div>
      </div>

      <!-- Sub-section C: Member process sketch -->
      <div class="social-proof__process">
        <h3 class="social-proof__process-heading">How a Member Uses Dataland</h3>
        <!-- [PLACEHOLDER] Replace with final process sketch before production -->
        <img
          src="/static/images/member-process-sketch.svg"
          alt="Diagram showing the Dataland member workflow: identify data gap, submit request, Dataland sources from public reports with AI and human review, structured data becomes available, member downloads or accesses via API"
          class="social-proof__process-image"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Section } from '@/types/ContentTypes';
import { SOCIAL_PROOF_QUOTES, SUCCESS_STORY_SUMMARIES } from '@/components/resources/landingPage/socialProofContent';
import { useBreakpoint } from '@/composables/useBreakpoint';
import SlideShow from '@/components/general/SlideShow.vue';

// sections prop is accepted for interface compatibility with the page orchestrator pattern
// (LandingPage.vue passes sections to all section components), but this component
// sources its content from socialProofContent.ts instead of content.json.
// eslint-disable-next-line vue/no-unused-properties
defineProps<{ sections?: Section[] }>();

const { width, isMobile } = useBreakpoint();
const isMobileView = computed(() => isMobile());
const quoteSlideWidth = computed(() => Math.min(width.value - 32, 320));
</script>

<style lang="scss">
.social-proof {
  padding: 5rem 2rem;
  background: var(--p-surface-0, #ffffff);

  &__wrapper {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 3rem;
  }

  &__heading {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  // Sub-section A: Quote cards -- Desktop 2x2 grid
  &__quotes-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 2rem;
    max-width: 1000px;
    margin: 0 auto;
    width: 100%;
  }

  // Sub-section A: Quote cards -- Mobile SlideShow
  &__quotes-mobile {
    overflow: hidden;
  }

  &__slider-wrapper {
    overflow: hidden;
  }

  &__slider-container {
    display: flex;
    transition: transform 0.3s ease-out;
    gap: 16px;

    &.isdragging .social-proof__quote-slide {
      cursor: grabbing;
    }
  }

  &__quote-slide {
    flex: 0 0 320px;
    cursor: grab;
  }

  &__slider-arrows {
    display: flex;
    justify-content: center;
    gap: 18px;
    margin-top: 1.5rem;
    touch-action: manipulation;
  }

  &__slider-arrow {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid rgba(203, 203, 203, 0.24);
    background-color: var(--default-neutral-white, #ffffff);
    cursor: pointer;

    &:hover {
      border: 2px solid #585858;
    }

    &--left,
    &--right {
      &::before {
        content: '';
        display: block;
        width: 24px;
        height: 24px;
        background-image: url('/static/icons/Arrow--right.svg');
        background-size: contain;
        background-repeat: no-repeat;
      }
    }

    &--left {
      &::before {
        transform: scaleX(-1);
      }
    }
  }

  // Shared quote card styles
  &__quote-card {
    background: var(--default-neutral-white, #ffffff);
    border-radius: 16px;
    box-shadow: 0 4px 32px 0 rgba(0, 0, 0, 0.08);
    padding: 40px;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  &__quote-mark {
    font-size: 48px;
    line-height: 1;
    color: var(--p-primary-color, #ff6813);
    opacity: 0.3;
    user-select: none;
  }

  &__quote-text {
    font-size: 20px;
    line-height: 28px;
    font-weight: 400;
    color: var(--p-text-color, #1b1b1b);
    margin: 0;
  }

  &__quote-attribution {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    margin-top: auto;
  }

  &__quote-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--p-text-color, #1b1b1b);
  }

  &__quote-role {
    font-size: 14px;
    font-weight: 400;
    color: var(--p-primary-color, #ff6813);
  }

  // Sub-section B: Success story cards
  &__stories-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 32px;
  }

  &__story-card {
    background: var(--default-neutral-white, #ffffff);
    border-radius: 12px;
    border: 1px solid var(--grey-tones-200, #e0e0e0);
    padding: 32px;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  &__story-title {
    font-size: 20px;
    font-weight: 600;
    color: var(--p-text-color, #1b1b1b);
    margin: 0;
  }

  &__story-summary {
    font-size: 16px;
    line-height: 24px;
    color: var(--grey-tones-600, #585858);
    margin: 0;
    flex: 1;
  }

  &__story-link {
    font-size: 14px;
    font-weight: 600;
    color: var(--p-primary-color, #ff6813);
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: auto;

    &:hover {
      text-decoration: underline;
    }

    i {
      font-size: 12px;
    }
  }

  // Sub-section C: Process sketch
  &__process {
    max-width: 900px;
    margin: 0 auto;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.5rem;
  }

  &__process-heading {
    font-size: 1.5rem;
    font-weight: 700;
    margin: 0;
    color: var(--p-text-color, #1b1b1b);
    text-align: center;
  }

  &__process-image {
    width: 100%;
    height: auto;
  }
}

.disabled {
  opacity: 0.5;
  pointer-events: none;
}

// Tablet: 2-column stories, 2-column quotes
@media only screen and (max-width: $bp-lg) {
  .social-proof {
    &__stories-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

// Mobile
@media only screen and (max-width: $bp-md) {
  .social-proof {
    padding: 2.5rem 1rem;

    &__wrapper {
      gap: 2rem;
    }

    &__heading {
      font-size: 1.5rem;
    }

    &__quote-card {
      padding: 24px;
    }

    &__quote-text {
      font-size: 18px;
      line-height: 26px;
    }

    &__stories-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    &__story-card {
      padding: 24px;
    }

    &__process {
      overflow-x: auto;
    }
  }
}
</style>
