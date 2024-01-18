<template>
  <footer :class="['footer', { 'footer--light': isLightVersion }]" role="contentinfo" data-test="dataland footer">
    <div class="footer__row footer__row--top">
      <div class="footer__section footer__section--logo">
        <img v-if="footerLogo" :src="footerLogo" alt="Dataland Logo" class="footer__logo" />
      </div>
      <div class="footer__section footer__section--columns" aria-labelledby="footer-navigation">
        <div
          class="footer__column"
          v-for="card in nonLegalCards"
          :key="card.title || 'default-key'"
          @click="card.title && toggleAccordion(card.title)"
        >
          <h3 :id="card.title" class="footer__column-title">
            {{ card.title }}
            <span class="footer__toggle-icon">
              {{ card.title && isAccordionOpen(card.title) ? "-" : "+" }}
            </span>
          </h3>
          <ul class="footer__column-list" v-show="!isSmallScreen || (card.title && isAccordionOpen(card.title))">
            <li v-for="link in card.links" :key="link.text">
              <a
                v-if="isExternalLink(link.url)"
                :href="link.url"
                class="footer__column-link"
                target="_blank"
                rel="noopener noreferrer"
                >{{ link.text }}</a
              >
              <router-link v-else :to="link.url" class="footer__column-link">{{ link.text }}</router-link>
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
import { computed, ref, onMounted, onUnmounted, type Ref } from "vue";
import type { Section } from "@/types/ContentTypes";

const props = defineProps<{
  sections?: Section[];
  isLightVersion: boolean;
}>();

const footerSection = computed(() => {
  return props.sections?.find((section) => section.title === "Footer") ?? null;
});

const openAccordions = ref<Record<string, boolean>>({});

/**
 * Toggles the open state of an accordion section based on its title.
 * If the title is already in the openAccordions set, it will be removed (closed),
 * otherwise, it will be added (opened).
 * @param {string | undefined} title - The title of the accordion section.
 */
function toggleAccordion(title: string | undefined): void {
  if (title) {
    openAccordions.value[title] = !openAccordions.value[title];
  }
}

const isAccordionOpen = (title: string | undefined): boolean => {
  return title ? openAccordions.value[title] || false : true;
};

const footerLogo = computed(() => footerSection.value?.image?.[0] ?? "");

const nonLegalCards = computed(() => footerSection.value?.cards?.filter((card) => card.title !== "Legal") ?? []);

const legalLinks = computed(() => {
  const legalCard = footerSection.value?.cards?.find((card) => card.title === "Legal");
  return legalCard?.links ?? [];
});

const isExternalLink = (url: string): boolean => {
  const externalUrlPattern = /^https?:\/\//;
  return externalUrlPattern.test(url);
};

const copyrightText = computed(() => {
  if (!footerSection.value?.text) return "";
  const currentYear = new Date().getFullYear();
  return `${footerSection.value.text[0]}${currentYear}${footerSection.value.text[1]}`;
});

const isSmallScreen: Ref<boolean> = ref(window.innerWidth < 768);

// Create a function to update `isSmallScreen` when the window is resized
const updateScreenSize = (): void => {
  isSmallScreen.value = window.innerWidth < 768;
};

onMounted(() => {
  // Add a resize event listener that updates `isSmallScreen`
  window.addEventListener("resize", updateScreenSize);
  // Initialize `isSmallScreen` value based on the current window width
  updateScreenSize();
});

onUnmounted(() => {
  // Remove the resize event listener when the component is unmounted
  window.removeEventListener("resize", updateScreenSize);
});
</script>

<style scoped lang="scss">
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
      align-items: center;
      img {
        height: auto;
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
    }
    li {
      margin-bottom: 16px;
    }
    &:not(:last-of-type) &-link::after {
      content: "";
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
        content: "";
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
    .footer__column {
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
@media only screen and (max-width: $medium) {
  .footer {
    &__row--top {
      flex-direction: column;
      gap: 3em;
    }
  }
}
@media only screen and (max-width: $small) {
  .footer {
    padding: 64px 24px 40px;

    &__row {
      flex-direction: column;
    }

    &__row--top,
    &__row--bottom {
      flex-direction: column;
    }

    &__section {
      width: 100%;

      &--logo {
        justify-content: left;
        margin-bottom: 3em;
      }

      &--columns {
        flex-direction: column;
        gap: 1.5em;
      }

      &--legal {
        justify-content: left;
        margin-top: 2em;
      }
    }

    &__column {
      align-items: center;
      text-align: left;

      &:not(:last-of-type) {
        margin-bottom: 1em;
      }

      &-title {
        margin-bottom: 1em;
        cursor: pointer;
        user-select: none;
      }
      .footer__column[aria-expanded="true"] .footer__toggle-icon {
        transform: rotate(45deg);
      }
      .footer__toggle-icon {
        transition: transform 0.3s ease;
      }
      &-list,
      &-link {
        width: 100%;
        justify-content: center;
      }
    }

    &__legal-list {
      flex-direction: column;
      align-items: flex-start;
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
