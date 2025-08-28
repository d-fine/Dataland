<template>
  <DocumentationCard :title="dataPoint.name" :item-id="dataPoint.dataPointType.id" id-label="ID">
    <slot />
  </DocumentationCard>
</template>

<script setup lang="ts">
import DocumentationCard from './DocumentationCard.vue';
import type { DataPoint } from './types';

interface Props {
  dataPoint: DataPoint;
}

defineProps<Props>();
</script>
