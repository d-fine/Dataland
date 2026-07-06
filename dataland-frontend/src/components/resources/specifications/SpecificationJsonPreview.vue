<template>
  <pre class="specification-json-preview" data-test="specificationJsonPreview">{{ formattedJson }}</pre>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  /**
   * The value to render as pretty-printed, read-only JSON (e.g. a data point base type's `example`).
   */
  value: unknown;
}>();

const formattedJson = computed(() => JSON.stringify(props.value, null, 2));
</script>

<style scoped>
.specification-json-preview {
  background: var(--p-surface-50);
  border-radius: var(--p-border-radius-xs);
  padding: var(--spacing-md);
  overflow-x: auto;
  text-align: left;
}
</style>
