<template>
  <DocumentationCard :title="framework.name" :item-id="framework.framework.id" id-label="Framework ID">
    <slot />
  </DocumentationCard>
</template>

<script setup lang="ts">
import DocumentationCard from './DocumentationCard.vue';
import type { Framework } from './types';

interface Props {
  framework: Framework;
}

defineProps<Props>();
</script>
