<template>
  <div class="documentation-section">
    <div class="section-header flex justify-between items-center" style="padding: var(--spacing-md)">
      <div class="flex-1">
        <h2 class="text-lg font-semibold text-gray-900">
          {{ title }}
        </h2>
        <div class="text-sm text-gray-600 mt-1">
          <strong>{{ idLabel }}:</strong> {{ itemId }}
        </div>
      </div>
    </div>

    <div class="section-content bg-white border-l-4 border-gray-200">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string;
  itemId: string;
  idLabel: string;
}

defineProps<Props>();
</script>

<style scoped>
.documentation-section {
  border: 1px solid var(--p-surface-200);
  border-radius: var(--p-border-radius-xs);
  background-color: var(--p-surface-0);
  overflow: hidden;
  box-shadow: var(--p-card-shadow);
}

.section-header {
  background-color: var(--p-surface-50);
  border-bottom: 1px solid var(--p-surface-200);
  font-weight: var(--font-weight-medium);
}

.section-content {
  background-color: var(--p-surface-0);
  border-left: var(--spacing-xs) solid var(--p-surface-300);
}
</style>
