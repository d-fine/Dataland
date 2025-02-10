<template>
  <footer :class="['footer', { 'footer--light': isLightVersion }]" role="contentinfo" data-test="dataland footer">
    <div class="footer__row footer__row--top">
      <div class="footer__section footer__section--logo">
        <img v-if="footerLogo" :src="footerLogo" alt="Dataland Logo" class="footer__logo" />
        <div v-if="ownedByCard && ownedByCard.links && ownedByCard.links.length > 0" class="footer__owned-by">
          <p class="footer__owned-by-title">{{ ownedByCard.text }}</p>
          <img :src="ownedByCard.icon" alt="Owned by Logo" class="footer__owned-by-logo" />
          <a :href="ownedByCard.links[0].url" target="_blank" rel="noopener noreferrer" class="footer__owned-by-link">
            {{ ownedByCard.links[0].text }}
          </a>
        </div>
      </div>
      <div class="footer__section footer__section--columns" aria-labelledby="footer-navigation">
        <div
          class="footer__column"
          v-for="card in nonLegalCards"
          :key="card.title || 'default-key'"
          @click="isSmallScreen && card.title && toggleAccordion(card.title)"
        >
          <h3 :id="card.title" class="footer__column-title">
            {{ card.title }}
            <span v-if="isSmallScreen" class="footer__toggle-icon">
              {{ card.title && isAccordionOpen(card.title) ? '-' : '+' }}
            </span>
          </h3>
          <ul
            class="footer__column-list"
            :style="{ maxHeight: !isSmallScreen || (card.title && isAccordionOpen(card.title)) ? '500px' : '0px' }"
          >
            <li v-for="link in card.links" :key="link.text">
              <a :href="link.url" class="footer__column-link" rel="noopener noreferrer">{{ link.text }}</a>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div class="footer__row footer__row--bottom">
      <div class="footer__section footer__section--legal">
        <ul class="footer__legal-list" v-if="legalLinks.length">
          <li v-for="link in legalLinks" :key="link.text">
            <router-link :to="link.url" class="footer__legal-list-link">{{ link.text }}</router-link>
          </li>
        </ul>
      </div>
      <div class="footer__copyright">
        {{ copyrightText }}
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, type Ref } from 'vue';
import type { Section } from '@/types/ContentTypes';

const props = defineProps<{
  sections?: Section[];
  isLightVersion: boolean;
}>();

const footerSection = computed(() => {
  return props.sections?.find((section) => section.title === 'Footer') ?? null;
});

const openAccordions = ref<Record<string, boolean>>({});

/**
 * Toggles the open state of an accordion section. If the accordion section
 * corresponding to the provided title is currently closed, it will be opened,
 * and vice versa.
 * @param {string} title - The title of the accordion section to toggle.
 */
function toggleAccordion(title: string): void {
  openAccordions.value[title] = !openAccordions.value[title];
}

const isAccordionOpen = (title: string | undefined): boolean => (title ? openAccordions.value[title] : false);

const footerLogo = computed(() => footerSection.value?.image?.[0] ?? '');

const ownedByCard = computed(() => {
  const card = footerSection.value?.cards?.find((card) => card.title === 'Owned by:');
  if (card && !card.links) {
    card.links = [];
  }
  return card;
});

const nonLegalCards = computed(() => {
  return footerSection.value?.cards?.filter((card) => card.title !== 'Legal' && card.title !== 'Owned by:') ?? [];
});

const legalLinks = computed(() => footerSection.value?.cards?.find((card) => card.title === 'Legal')?.links ?? []);

const copyrightText = computed(() => {
  if (!footerSection.value?.text) return '';
  const currentYear = new Date().getFullYear();
  return `${footerSection.value.text[0]}${currentYear}${footerSection.value.text[1]}`;
});

const isSmallScreen: Ref<boolean> = ref(window.innerWidth < 768);

const updateScreenSize = (): void => {
  isSmallScreen.value = window.innerWidth < 768;
};

onMounted(() => {
  window.addEventListener('resize', updateScreenSize);
  updateScreenSize();
});

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenSize);
});
</script>

<style scoped lang="scss">
@use '@/assets/scss/newVariables';

.footer {
  background-color: var(--basic-dark);
  color: var(--default-neutral-white);
  padding: 96px 64px 48px;
  &__row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 40px;
    &--bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 40px;
      border-top: 2px solid var(--grey-tones-900);
      margin-top: 16px;
    }
  }

  &__section {
    &--logo {
      flex: 1;
      display: flex;
      align-items: flex-start;
      flex-direction: column;
      text-align: left;
      gap: 1em;
      img {
        height: auto;
      }
      .footer__owned-by {
        display: flex;
        flex-direction: column;
        gap: 1em;
        &-title {
          color: var(--grey-tones-300);
          font-size: 14px;
          font-style: normal;
          font-weight: 400;
          line-height: 20px;
          letter-spacing: 0.25px;
          margin: 0;
        }

        &-logo {
          width: 79px;
          filter: invert(1);
        }

        &-link {
          color: var(--default-neutral-white);
          font-weight: 600;
          line-height: 24px;
          letter-spacing: 0.75px;
          text-transform: uppercase;
          text-decoration: none;
          &:hover {
            text-decoration: underline;
            text-underline-offset: 4px;
            text-decoration-thickness: 2px;
          }
          &::after {
            content: '';
            display: inline-block;
            top: 3px;
            width: 16px;
            height: 16px;
            background-image: url(/static/icons/Arrow--up-right.svg);
            background-size: cover;
            position: relative;
            margin-left: 8px;
            filter: invert(1);
          }
        }
      }
    }

    &--columns {
      flex: 3;
      display: flex;
      justify-content: flex-end;
      gap: 96px;
      text-align: left;
    }

    &--legal {
      flex: 1;
      display: flex;
      justify-content: flex-start;
    }
  }

  &__column {
    &-title {
      color: var(--grey-tones-300);
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
      letter-spacing: 0.25px;
      margin: 0 0 28px;
    }
    &-list {
      list-style: none;
      padding-left: 0;
      margin: 0;
      max-height: 500px;
      overflow: hidden;
      transition: max-height 0.4s ease;
    }
    .footer__toggle-icon {
      transition: transform 0.4s ease;
    }
    li {
      margin-bottom: 16px;
    }
    &:not(:last-of-type) &-link::after {
      content: '';
      display: inline-block;
      top: 3px;
      width: 16px;
      height: 16px;
      background-image: url(/static/icons/Arrow--up-right.svg);
      background-size: cover;
      position: relative;
      margin-left: 8px;
      filter: invert(1);
    }
    &:last-of-type &-link {
      top: -3px;
      position: relative;
      &::before {
        content: '';
        display: inline-block;
        top: 3px;
        width: 24px;
        height: 21px;
        background-image: url(/static/icons/Logo--linkedin.svg);
        background-size: cover;
        position: relative;
        margin-right: 6px;
      }
    }
    &-link {
      color: var(--default-neutral-white);
      font-weight: 600;
      line-height: 24px;
      letter-spacing: 0.75px;
      text-transform: uppercase;
      text-decoration: none;
      &:hover {
        text-decoration: underline;
        text-underline-offset: 4px;
        text-decoration-thickness: 2px;
      }
    }
  }

  &__legal-list {
    display: flex;
    list-style: none;
    padding-left: 0;
    margin: 0;
    gap: 48px;
    &-link {
      color: var(--default-neutral-white);
      font-weight: 600;
      line-height: 24px;
      letter-spacing: 0.75px;
      text-transform: uppercase;
      text-decoration: none;
      &:hover {
        text-decoration: underline;
        text-underline-offset: 4px;
        text-decoration-thickness: 2px;
      }
    }
  }

  &__copyright {
    font-weight: 400;
    line-height: normal;
    letter-spacing: 0.25px;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: right;
    flex: 1;
  }
  // Modifier class for light version
  &--light {
    background-color: #f6f5f0;
    color: #0b191f;
    .footer__column,
    .footer__owned-by {
      &-list {
        max-height: 500px;
      }
      &-logo {
        filter: invert(0);
      }
      &-title,
      &-link {
        color: #0b191f;
        &::after {
          filter: invert(0) !important;
        }
        &::before {
          filter: invert(1);
        }
      }
    }
    .footer__legal-list-link {
      color: #0b191f;
    }
  }
}
@media only screen and (max-width: newVariables.$medium) {
  .footer {
    &__row--top {
      flex-direction: column;
      gap: 3em;
    }
  }
}
@media only screen and (max-width: newVariables.$small) {
  .footer {
    padding: 64px 24px 40px;

    &__row {
      flex-direction: column;
    }

    &__row--top,
    &__row--bottom {
      flex-direction: column;
      gap: 0;
      border: 0;
      padding: 0;
      margin: 0;
    }
    &__row--top {
      margin-bottom: 2em;
    }

    &__section {
      width: 100%;

      &--logo {
        justify-content: left;
        margin-bottom: 1.5em;
        .footer__owned-by {
          gap: 0.5em;
        }
      }

      &--columns {
        flex-direction: column;
        gap: 0;
      }

      &--legal {
        justify-content: left;
      }
    }

    &__column {
      align-items: center;
      text-align: left;
      border-bottom: 1px solid var(--grey-tones-900);

      &-title {
        margin: 1.5em 0;
        cursor: pointer;
        user-select: none;
        font-size: 1em;
        font-weight: 400;
        line-height: 24px; /* 150% */
        letter-spacing: 0.25px;
        color: var(--default-neutral-white);
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
      .footer__toggle-icon[data-v-28e6a421] {
        transition: transform 0.4s ease;
        font-size: 1.3em;
        line-height: 1.5em;
      }
      .footer__column[aria-expanded='true'] .footer__toggle-icon {
        transform: rotate(45deg);
      }
      &-list,
      &-link {
        width: 100%;
        justify-content: center;
      }
      &-list li {
        margin-bottom: 1.5em;
      }
    }

    &__legal-list {
      flex-direction: column;
      align-items: flex-start;
      gap: 2em;
    }
    &__copyright {
      text-align: left;
      flex: 1;
      width: 100%;
      margin-top: 2em;
    }
  }
}
</style>
