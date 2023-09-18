<template>
  <nav class="header__nav" aria-label="Main Navigation">
    <ul class="header__nav-list" role="menu">
      <li
        class="header__nav-item"
        v-for="page in filteredPages"
        :key="page.url"
        :class="{ active: route.path === page.url }"
      >
        <router-link :to="page.url" role="menuitem" tabindex="0" class="header__nav-link" :aria-label="page.title">{{
          page.title
        }}</router-link>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import type { Content } from "@/types/ContentTypes";

const route = useRoute();
const { contentData } = defineProps<{ contentData: Content }>();

const filteredPages = computed(() => {
  return contentData.pages.filter((page) => ["/mission", "/community", "/campaigns"].includes(page.url));
});
</script>

<style scoped lang="scss">
// @import "../../assets/scss/newVariables";

#app.lp {
  .header {
    &__nav {
      flex-grow: 1;
      display: flex;
      justify-content: center;
      align-items: center;

      &-list {
        list-style: none;
        display: flex;
        gap: 48px;
        padding: 0;
        margin: 0;
        .header__nav-item.active .header__nav-link {
          border-bottom: 2px solid var(--primary-orange);
          color: var(--primary-orange);
        }
      }

      &-link {
        position: relative;
        text-decoration: none;
        color: var(--basic-dark);
        font-size: 16px;
        font-style: normal;
        font-weight: 600;
        line-height: 20px;
        letter-spacing: 0.75px;
        text-transform: uppercase;

        &:hover {
          border-bottom: 2px solid var(--basic-dark);

          &::before {
            content: "";
            display: block;
            position: absolute;
            left: -20px;
            top: 50%;
            transform: translateY(-50%);
            width: 16px;
            height: 16px;
            background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none"><rect width="16" height="16" fill="white" fill-opacity="0.01" style="mix-blend-mode:multiply"/><path d="M9.14142 2.85858L9.00184 2.719L8.86044 2.85674L8.14544 3.55324L8.00055 3.69438L8.14333 3.83767L11.5934 7.3H2H1.8V7.5V8.5V8.7H2H11.5918L8.14364 12.145L8.00194 12.2866L8.14373 12.4281L8.85873 13.1416L9.00015 13.2827L9.14142 13.1414L14.1414 8.14142L14.2828 8L14.1414 7.85858L9.14142 2.85858Z" fill="%231B1B1B" stroke="%231B1B1B" stroke-width="0.4"/></svg>');
            background-size: cover;
          }
        }
      }
    }
  }
}
</style>
