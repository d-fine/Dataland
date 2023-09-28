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
            background-image: url("/static/icons/Arrow--right.svg");
            background-size: cover;
          }
        }
      }
    }
  }
}
</style>
