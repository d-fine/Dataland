<template>
  <DocumentationCard :title="baseType.name" :item-id="baseType.dataPointBaseType.id" id-label="Base Type ID">
    <slot />
  </DocumentationCard>
</template>

<script setup lang="ts">
import DocumentationCard from './DocumentationCard.vue';
import type { DataPointBaseType } from './types';

interface Props {
  baseType: DataPointBaseType;
}

defineProps<Props>();
</script>
