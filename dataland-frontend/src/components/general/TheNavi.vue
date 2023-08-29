<template>
  <nav aria-label="Main Navigation">
    <ul role="menu">
      <li v-for="page in filteredPages" :key="page.url" role="none">
        <router-link :to="page.url" role="menuitem" tabindex="0">{{ page.title }}</router-link>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { Content } from "@/types/ContentTypes";

const { contentData } = defineProps<{ contentData: Content }>();

const filteredPages = computed(() => {
  return contentData.pages.filter((page) => ["/campaigns", "/about", "/features"].includes(page.url));
});
</script>

<style scoped></style>
