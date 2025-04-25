<template>
  <footer :class="['footer', { 'footer--light': isLightVersion }]" role="contentinfo" data-test="dataland footer">
    <div class="footer__row footer__row--top">
      <div class="footer__section footer__section--owned-by">
        <img src="/static/logos/gfx_logo_dataland_orange_S.svg" alt="Dataland Logo" class="footer__logo" />
        <p class="footer--title footer--small-title">A Werte-Stiftung Company</p>
        <img src="/static/logos/footer_wertestiftung.png" alt="Owned by Logo" class="footer__section--logo" />
        <a href="https://www.wertestiftung.org" target="_blank" rel="noopener noreferrer" class="footer--link"
          >WERTESTIFTUNG.ORG</a
        >
      </div>

      <div class="footer__section footer__section--columns" aria-labelledby="footer-navigation">
        <div class="footer__column footer__column--techhub" @click="isSmallScreen && toggleAccordion('techhub')">
          <h3 class="footer--title">
            <span>Tech Hub</span>
            <span v-if="isSmallScreen" class="footer__toggle-icon"> {{ isAccordionOpen('techhub') ? '-' : '+' }} </span>
          </h3>
          <ul
            class="footer__column--list"
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('techhub') ? '500px' : '0' }"
          >
            <li v-for="link in techHubLinks" :key="link.text">
              <a :href="link.url" class="footer--link" rel="noopener noreferrer">{{ link.text }}</a>
            </li>
          </ul>
        </div>
        <div class="footer__column" @click="isSmallScreen && toggleAccordion('esgframeworks')">
          <h3 class="footer--title">
            <span> ESG Frameworks</span>
            <span v-if="isSmallScreen" class="footer__toggle-icon">
              {{ isAccordionOpen('esgframeworks') ? '-' : '+' }}
            </span>
          </h3>
          <ul
            class="footer__column--list"
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('esgframeworks') ? '500px' : '0' }"
          >
            <li>
              <a
                href="https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation"
                class="footer--link"
                rel="noopener noreferrer"
                >OVERVIEW</a
              >
            </li>
          </ul>
        </div>
        <div class="footer__column footer__column--follow" @click="isSmallScreen && toggleAccordion('follow')">
          <h3 class="footer--title">
            Follow Dataland
            <span v-if="isSmallScreen" class="footer__toggle-icon"> {{ isAccordionOpen('follow') ? '-' : '+' }} </span>
          </h3>
          <ul
            class="footer__column--list"
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('follow') ? '500px' : '0px' }"
          >
            <li>
              <a
                href="https://www.linkedin.com/company/dataland-gmbh/"
                class="footer--link linkedin"
                rel="noopener noreferrer"
                >LINKEDIN
              </a>
            </li>
          </ul>
        </div>
        <div
          class="footer__column footer__column--integration"
          @click="isSmallScreen && toggleAccordion('integration')"
        >
          <h3 class="footer--title">
            Integration of GLEIF and EuroDaT
            <span v-if="isSmallScreen" class="footer__toggle-icon">
              {{ isAccordionOpen('integration') ? '-' : '+' }}
            </span>
          </h3>
          <ul
            class="footer__column--list inline-flex gap-5"
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('integration') ? '500px' : '0px' }"
          >
            <li><img src="/static/logos/footer_gleif.png" alt="GLEIF Logo" class="footer__section--logo" /></li>
            <li><img src="/static/logos/footer_eurodat.png" alt="EuroDaT Logo" class="footer__section--logo" /></li>
          </ul>
        </div>
      </div>
    </div>
    <div class="footer__row footer__row--bottom">
      <div class="footer__section footer__section--legal">
        <ul>
          <li v-for="link in legalLinks" :key="link.text">
            <router-link :to="link.url">{{ link.text }}</router-link>
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
type Link = {
  text: string;
  url: string;
};

import { computed, onMounted, onUnmounted, ref, type Ref } from 'vue';

const { isLightVersion } = defineProps({
  isLightVersion: {
    type: Boolean,
    required: false,
    default: true,
  },
});

const legalLinks: Link[] = [
  {
    text: 'LEGAL',
    url: '/terms',
  },
  {
    text: 'IMPRINT',
    url: '/imprint',
  },
  {
    text: 'DATA PRIVACY',
    url: '/dataprivacy',
  },
];

const techHubLinks: Link[] = [
  {
    text: 'DATASETS',
    url: '/api/swagger-ui/index.html',
  },
  {
    text: 'DOCUMENTS',
    url: '/documents/swagger-ui/index.html',
  },
  {
    text: 'COMMUNITY',
    url: '/community/swagger-ui/index.html',
  },
  {
    text: 'QUALITY ASSURANCE',
    url: '/qa/swagger-ui/index.html',
  },
  {
    text: 'USERS',
    url: '/users/swagger-ui/index.html',
  },
];

const copyrightText = computed(() => {
  const currentYear = new Date().getFullYear();
  return `Copyright © ${currentYear} Dataland`;
});

const openAccordions = ref<Record<string, boolean>>({});

/**
 * Reads from the 'openAccordions' record if the accordion with the title passed as input is open or not
 * @param title
 */
const isAccordionOpen = (title: string | undefined): boolean => (title ? openAccordions.value[title] : false);

/**
 * Toggles the open state of an accordion section. If the accordion section
 * corresponding to the provided title is currently closed, it will be opened,
 * and vice versa.
 * @param {string} title - The title of the accordion section to toggle.
 */
function toggleAccordion(title: string): void {
  openAccordions.value[title] = !openAccordions.value[title];
}

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
  padding: 6rem 4rem 3rem;

  &--title {
    color: var(--grey-tones-300);
    font-size: 0.875rem;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
    letter-spacing: 0.25px;
    margin: 0;
  }

  &--link {
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
      width: 1rem;
      height: 1rem;
      background-image: url(/static/icons/Arrow--up-right.svg);
      background-size: cover;
      position: relative;
      margin-left: 0.5rem;
      filter: invert(1);
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

  &__row--top {
    padding-bottom: 2.5rem;
    display: grid;
    grid-template-columns: 60% 40%;
  }

  &__row--bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 2.5rem 0;
    border-top: 2px solid var(--grey-tones-900);
  }

  &__section {
    &--owned-by {
      display: flex;
      align-items: flex-start;
      flex-direction: column;
      text-align: left;
      gap: 1rem;
    }

    &--columns {
      display: grid;
      grid-template-columns: 1.5fr 1fr 1fr;
      grid-template-rows: repeat(2, 1fr);
      justify-content: space-between;
      justify-items: end;
      text-align: start;
      gap: 1rem 0;
    }

    &--logo {
      max-height: 39px;
      filter: invert(1);
    }

    &--legal {
      ul {
        display: flex;
        list-style: none;
        padding-left: 0;
        margin: 0;
        gap: 3rem;
      }

      a {
        color: var(--default-neutral-white);
        font-weight: 600;
        line-height: 1.5rem;
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
  }

  &__column {
    &--list {
      list-style: none;
      padding-left: 0;
      margin: 1.75rem 0 0 0;
      max-height: 500px;
      overflow: hidden;
      transition: max-height 0.4s ease;
    }

    li {
      margin-bottom: 1rem;
    }

    &--techhub {
      grid-row: span 2;
    }

    &--integration {
      grid-column: span 2;
      justify-self: center;
    }

    &--follow {
      & .linkedin {
        position: relative;
        top: -3px;

        &::before {
          content: '';
          display: inline-block;
          top: 3px;
          width: 1.5rem;
          height: 21px;
          background-image: url(/static/icons/Logo--linkedin.svg);
          background-size: cover;
          position: relative;
          margin-right: 6px;
        }

        &::after {
          content: none;
        }
      }
    }

    .footer__toggle-icon {
      transition: transform 0.4s ease;
    }
  }

  // Modifier class for light version
  &--light {
    background-color: #f6f5f0;
    color: #0b191f;

    .footer {
      &__section--logo {
        filter: invert(0);
      }

      &--title,
      &--link {
        color: #0b191f;

        &::after {
          filter: invert(0);
        }

        &::before {
          filter: invert(1);
        }
      }

      &__section--legal a {
        color: #0b191f;
      }
    }
  }
}

@media only screen and (max-width: newVariables.$medium) {
  .footer {
    &__row--top {
      display: flex;
      flex-direction: column;
      gap: 3rem;
    }

    &__section--columns {
      justify-items: start;
    }
  }
}

@media only screen and (max-width: newVariables.$small) {
  .footer {
    padding: 4rem 1.5rem 2.5rem;

    &--title {
      margin: 0;
      font-size: 1rem;
      font-weight: 400;
      color: var(--default-neutral-white);
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    &--small-title {
      font-size: 0.875rem;
      color: var(--grey-tones-300);
    }

    &--logo {
      justify-content: left;
      margin-bottom: 1.5em;

      .footer__owned-by {
        gap: 0.5em;
      }
    }

    &__row--top,
    &__row--bottom {
      display: flex;
      flex-direction: column;
      align-items: start;
      gap: 1rem;
      border: 0;
      padding-bottom: 0;
    }

    &__section {
      &--owned-by,
      &--columns {
        display: flex;
        flex-direction: column;
        text-align: center;
        width: 100%;
        gap: 0;
      }

      &--owned-by {
        gap: 0.5rem;
      }

      &--legal ul {
        flex-direction: column;
        align-items: flex-start;
        gap: 2em;
      }
    }

    &__column {
      display: flex;
      flex-wrap: wrap;
      width: 100%;
      justify-content: space-between;
      cursor: pointer;
      user-select: none;
      margin: 0;
      border-bottom: 1px solid var(--grey-tones-900);

      .footer--title {
        padding: 1.75rem 0;
      }

      &--list {
        width: 100%;
        justify-content: start;
        justify-items: start;
        margin: 0;
      }

      .footer__toggle-icon {
        transition: transform 0.4s ease;
      }
    }
    &__copyright {
      margin-top: 2rem;
    }
  }
}
</style>
