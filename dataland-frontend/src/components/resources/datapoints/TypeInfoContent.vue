<template>
  <div style="font-size: var(--font-size-sm)">
    <div><strong>ID:</strong> {{ typeInfo.id }}</div>
    <div v-if="typeInfo.ref" style="margin-top: var(--spacing-xs)">
      <strong>Reference:</strong>
      <ExternalLink :href="typeInfo.ref" style="color: var(--p-primary-color)">{{ typeInfo.ref }}</ExternalLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import ExternalLink from './ExternalLink.vue';
import type { DataPointType } from './types';

interface Props {
  typeInfo: DataPointType;
}

defineProps<Props>();
</script>
