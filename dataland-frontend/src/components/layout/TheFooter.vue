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
      <router-link to="/legal" class="footer__right-link" aria-label="Legal">Legal</router-link>
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
  return sections?.find((section) => section.title === "Footer") || null;
});

const footerText = computed(() => {
  if (!footerSection.value || !footerSection.value.text) return "";
  const currentYear = new Date().getFullYear();
  return `${footerSection.value.text[0]}${currentYear}${footerSection.value.text[1]}`;
});
</script>

<style scoped lang="scss">
.footer {
  display: flex;
  justify-content: space-between;
  background-color: #1b1b1b;
  color: #fff;
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
    justify-content: space-between;
    width: 320px;

    &-link {
      font-size: 16px;
      font-style: normal;
      font-weight: 600;
      line-height: 24px; /* 150% */
      letter-spacing: 0.75px;
      text-transform: uppercase;
      text-decoration: none;
      color: #fff;
      border-bottom: 2px solid transparent;
      width: -moz-fit-content;
      width: -webkit-fit-content;
      width: fit-content;

      &:hover {
        border-bottom: 2px solid #fff;
      }
    }
  }
}
</style>
