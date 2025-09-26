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
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('techhub') ? '30rem' : '0' }"
          >
            <li><a href="/api/swagger-ui/index.html" class="footer--link" rel="noopener noreferrer">DATASETS</a></li>
            <li>
              <a href="/documents/swagger-ui/index.html" class="footer--link" rel="noopener noreferrer">DOCUMENTS</a>
            </li>
            <li>
              <a href="/community/swagger-ui/index.html" class="footer--link" rel="noopener noreferrer">COMMUNITY</a>
            </li>
            <li>
              <a href="/qa/swagger-ui/index.html" class="footer--link" rel="noopener noreferrer">QUALITY ASSURANCE</a>
            </li>
            <li><a href="/users/swagger-ui/index.html" class="footer--link" rel="noopener noreferrer">USERS</a></li>
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
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('esgframeworks') ? '30rem' : '0' }"
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
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('follow') ? '30rem' : '0' }"
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
            :style="{ maxHeight: !isSmallScreen || isAccordionOpen('integration') ? '30rem' : '0' }"
          >
            <li>
              <img
                src="/static/logos/footer_gleif.svg"
                alt="GLEIF Logo"
                class="footer__section--logo"
                style="height: 2.5rem"
              />
            </li>
            <li>
              <img
                src="/static/logos/footer_eurodat.svg"
                alt="EuroDaT Logo"
                class="footer__section--logo"
                style="height: 2.5rem"
              />
            </li>
          </ul>
        </div>
      </div>
    </div>
    <div class="footer__row footer__row--bottom">
      <div class="footer__section footer__section--legal">
        <ul>
          <li><router-link to="/terms">LEGAL</router-link></li>
          <li><router-link to="/imprint">IMPRINT</router-link></li>
          <li><router-link to="/dataprivacy">DATA PRIVACY</router-link></li>
        </ul>
      </div>
      <div class="footer__copyright">
        {{ `Copyright © ${new Date().getFullYear()} Dataland` }}
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, type Ref } from 'vue';

const { isLightVersion } = defineProps({
  isLightVersion: {
    type: Boolean,
    required: false,
    default: true,
  },
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

const isSmallScreen: Ref<boolean> = ref(globalThis.innerWidth < 768);

const updateScreenSize = (): void => {
  isSmallScreen.value = globalThis.innerWidth < 768;
};

onMounted(() => {
  globalThis.addEventListener('resize', updateScreenSize);
  updateScreenSize();
});

onUnmounted(() => {
  globalThis.removeEventListener('resize', updateScreenSize);
});
</script>

<style scoped lang="scss">
* {
  margin: 0;
  font-style: normal;
  font-weight: 400;
  line-height: normal;
}

.footer {
  background-color: var(--p-gray-950);
  color: var(--default-neutral-white);
  padding: 6rem 4rem 3rem;

  &--title {
    color: var(--grey-tones-300);
    font-size: 0.875rem;
  }

  &--link {
    color: var(--default-neutral-white);
    font-weight: 600;
    line-height: 1.5rem;
    letter-spacing: 0.05rem;
    text-transform: uppercase;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
      text-underline-offset: 0.25rem;
      text-decoration-thickness: 0.125rem;
    }

    &::after {
      content: '';
      display: inline-block;
      top: 0.1875rem;
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
    border-top: 0.125rem solid var(--grey-tones-900);
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
      grid-template-columns: 2fr 2fr 1fr;
      grid-template-rows: repeat(2, 1fr);
      justify-content: space-between;
      justify-items: start;
      text-align: start;
      gap: 1rem 0;
    }

    &--logo {
      filter: invert(1);
    }

    &--legal {
      ul {
        display: flex;
        list-style: none;
        padding-left: 0;
        gap: 3rem;
      }

      a {
        color: var(--default-neutral-white);
        font-weight: 600;
        line-height: 1.5rem;
        letter-spacing: 0.05rem;
        text-transform: uppercase;
        text-decoration: none;

        &:hover {
          text-decoration: underline;
          text-underline-offset: 0.25rem;
          text-decoration-thickness: 0.125rem;
        }
      }
    }
  }

  &__column {
    &--list {
      list-style: none;
      padding-left: 0;
      margin: 1.75rem 0 0 0;
      max-height: 30rem;
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
    }

    &--follow {
      & .linkedin {
        position: relative;
        top: -0.1875rem;

        &::before {
          content: '';
          display: inline-block;
          top: 0.1875rem;
          width: 1.5rem;
          height: 1.3125rem;
          background-image: url(/static/icons/Logo--linkedin.svg);
          background-size: cover;
          position: relative;
          margin-right: 0.375rem;
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
    background-color: var(--p-surface-100);
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

@media only screen and (max-width: 1024px) {
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

@media only screen and (max-width: 768px) {
  .footer {
    padding: 4rem 1.5rem 2.5rem;

    &--title {
      font-size: 1rem;
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
      border-bottom: 1px solid var(--grey-tones-900);

      .footer--title {
        padding: 1.75rem 0;
      }

      &--list {
        width: 100%;
        justify-content: start;
        justify-items: start;
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
