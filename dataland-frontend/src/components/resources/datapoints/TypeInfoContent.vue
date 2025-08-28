<template>
  <div>
    <div><strong>ID:</strong> {{ typeInfo.id }}</div>
    <div v-if="typeInfo.ref">
      <strong>Reference:</strong>
      <ExternalLink :href="typeInfo.ref" />
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
