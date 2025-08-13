<template>
  <router-link
    v-if="isInternalRoute"
    :to="documentationRoute"
    class="external-link internal-link"
    @mouseover="addUnderline"
    @mouseout="removeUnderline"
  >
    <slot>{{ documentationRoute }}</slot>
  </router-link>
  <a
    v-else
    :href="href"
    target="_blank"
    rel="noopener noreferrer"
    class="external-link"
    @mouseover="addUnderline"
    @mouseout="removeUnderline"
  >
    <slot>{{ href }}</slot>
  </a>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { convertSpecificationUrlToDocumentationRoute, isSpecificationServiceUrl } from '@/utils/DocumentationUrlConverter';

interface Props {
  href: string;
}

const props = defineProps<Props>();

const documentationRoute = computed(() => {
  return convertSpecificationUrlToDocumentationRoute(props.href);
});

const isInternalRoute = computed(() => {
  return isSpecificationServiceUrl(props.href);
});

const addUnderline = (event: MouseEvent) => {
  (event.target as HTMLElement).style.textDecoration = 'underline';
};

const removeUnderline = (event: MouseEvent) => {
  (event.target as HTMLElement).style.textDecoration = 'none';
};
</script>

<style scoped>
.external-link {
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

.internal-link {
  color: var(--p-primary-color);
}

.internal-link:hover {
  color: var(--p-primary-700);
}
</style>
