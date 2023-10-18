<template>
  <footer class="footer">
    <div class="footer__left">
      <div class="footer__logo" role="img" aria-label="Footer Logo">
        <img
          v-for="(img, index) in footerSection?.image ?? []"
          :key="index"
          :src="img"
          :alt="footerSection?.text.join(' ')"
          class="footer__logo-img"
        />
      </div>
      <div class="footer__copyright">{{ footerText }}</div>
    </div>

    <nav class="footer__right" role="navigation" aria-label="Footer Navigation">
      <router-link to="/imprint" class="footer__right-link" aria-label="Imprint">Imprint</router-link>
      <router-link to="/dataprivacy" class="footer__right-link" aria-label="Data Privacy">Data Privacy</router-link>
    </nav>
  </footer>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Section } from "@/types/ContentTypes";

const { sections } = defineProps<{ sections?: Section[] }>();

const footerSection = computed(() => {
  return sections?.find((section) => section.title === "Footer") ?? null;
});

const footerText = computed(() => {
  if (!footerSection.value?.text) return "";
  const currentYear = new Date().getFullYear();
  return `${footerSection.value.text[0]}${currentYear}${footerSection.value.text[1]}`;
});
</script>

<style scoped lang="scss">
.footer {
  display: flex;
  justify-content: space-between;
  background-color: var(--basic-dark);
  color: var(--default-neutral-white);
  padding: 80px 120px 140px;

  &__logo-img {
    margin-bottom: 32px;
    height: 32px;
  }

  &__copyright {
    font-size: 16px;
    font-style: normal;
    font-weight: 400;
    line-height: normal;
    letter-spacing: 0.25px;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  &__right {
    display: flex;
    flex-direction: column;
    height: 108px;
    justify-content: center;
    width: 320px;

    &-link {
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 32px;
      letter-spacing: 0.75px;
      text-transform: uppercase;
      text-decoration: none;
      color: var(--default-neutral-white);
      border-bottom: 2px solid transparent;
      width: -moz-fit-content;
      width: -webkit-fit-content;
      width: fit-content;

      &:hover {
        border-bottom: 2px solid var(--default-neutral-white);
      }
    }
  }
}
@media only screen and (max-width: $large) {
  .footer {
    grid-template-columns: repeat(12, 1fr);
    gap: 48px 22px;
    display: grid;
    padding: 80px 22px 140px;
    &__left {
      grid-column: 1 / 6;
    }
    &__right {
      grid-column: 7 / -1;
    }
  }
}
@media only screen and (max-width: $small) {
  .footer {
    padding: 80px 16px;
    gap: 64px 16px;
    grid-template-columns: 1fr;

    &__left {
      grid-row: 2;
      grid-column: 1;
      text-align: left;
    }
    &__right {
      grid-row: 1;
      grid-column: 1;
    }
  }
}
</style>
