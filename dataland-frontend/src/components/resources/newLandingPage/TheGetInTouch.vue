<template>
  <section
    v-if="getInTouchSection && getInTouchSection.cards"
    class="getintouch"
    role="region"
    aria-labelledby="getintouch-heading"
  >
    <div class="getintouch__wrapper">
      <h2 id="getintouch-heading" class="getintouch__title">
        {{ getInTouchSection.text[0] }}
        <span v-if="getInTouchSection.text[1]"> {{ getInTouchSection.text[1] }}</span>
      </h2>
      <div v-if="getInTouchSection.cards[1]" class="getintouch__image" role="img" aria-label="Contact Image">
        <img :src="getInTouchSection.cards[1].icon" :alt="getInTouchSection.cards[1].title" />
      </div>
      <div class="getintouch__text" grid-column="15 / 17">
        <p class="getintouch__text-title">{{ getInTouchSection.cards[1].title }}</p>
        <p class="getintouch__text-text">{{ getInTouchSection.cards[1].text }}</p>
        <ButtonComponent
          label="Get In Touch"
          buttonType="button-component getintouch__text-button"
          ariaLabel="Get In Touch"
          @click="() => openEmailClient(getInTouchSection?.cards?.[3])"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Section } from '@/types/ContentTypes';
import { openEmailClient } from '@/utils/Email';
import ButtonComponent from '@/components/resources/newLandingPage/ButtonComponent.vue';

const { sections } = defineProps<{ sections?: Section[] }>();

const getInTouchSection = computed(() => {
  return sections?.find((section) => section.title === 'Get in touch') ?? null;
});
</script>

<style scoped lang="scss">
.getintouch {
  padding: 140px 0 200px;
  align-items: center;
  display: flex;
  flex-direction: column;
  background-color: #1f1f1f;

  &__wrapper {
    display: grid;
    grid-template-columns: repeat(16, 1fr);
    gap: 48px 32px;
    max-width: 1440px;
    padding: 0 32px;
  }

  &__title {
    font-size: 64px;
    font-weight: 700;
    line-height: 78px;
    grid-column: 5 / 10;
    color: var(--default-neutral-white);
    text-align: left;
    margin: 0;
  }

  &__image {
    grid-column: 5 / 8;
    img {
      width: 100%;
      max-height: 320px;
      border-radius: 16px;
    }
  }

  &__text {
    grid-column: 8 / 14;
    text-align: left;
    font-size: 22.123px;
    font-style: normal;
    font-weight: 600;
    line-height: 33.185px; /* 150% */
    letter-spacing: 0.346px;
    color: var(--default-neutral-white);
    display: flex;
    flex-direction: column;
    justify-content: center;
    p {
      margin: 0;
    }
    &-text {
      color: var(--primary-orange);
    }
    &-button {
      margin-top: 32px;
      width: fit-content;
    }
  }
}
@media only screen and (max-width: $large) {
  .getintouch {
    &__wrapper {
      grid-template-columns: repeat(12, 1fr);
      gap: 48px 22px;
      padding: 0 22px;
      width: 100%;
    }
    &__title {
      grid-column: 3 /11;
    }

    &__image {
      grid-column: 3 / 6;
    }

    &__text {
      grid-column: 6 / 11;
    }
  }
}
@media only screen and (max-width: $medium) {
  .getintouch {
    padding: 80px 0;
    &__image {
      grid-column: 3 / 7;
    }

    &__text {
      grid-column: 7 / 11;
    }
  }
}
@media only screen and (max-width: $small) {
  .getintouch {
    &__wrapper {
      gap: 24px 16px;
      padding: 0 16px;
    }
    &__title {
      grid-column: 1 / -1;
      font-size: 40px;
    }
    &__image {
      grid-column: 1 / -1;
      text-align: left;
      img {
        max-width: 320px;
      }
    }

    &__text {
      grid-column: 1 / -1;
    }
  }
}
</style>
