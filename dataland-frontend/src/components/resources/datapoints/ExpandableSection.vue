<template>
  <div class="subsection">
    <div
      @click="toggleExpanded"
      class="subsection-header cursor-pointer flex justify-between items-start"
      style="padding: var(--spacing-sm)"
    >
      <h3 class="font-medium text-gray-800 flex-1">{{ title }}</h3>
      <i :class="isExpanded ? 'pi pi-chevron-down' : 'pi pi-chevron-right'" class="text-gray-500 chevron-icon ml-4"></i>
    </div>
    <div v-show="isExpanded" class="subsection-content" style="padding: var(--spacing-md)">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string;
  isExpanded: boolean;
}

interface Emits {
  (e: 'toggle'): void;
}

defineProps<Props>();
const emit = defineEmits<Emits>();

const toggleExpanded = () => {
  emit('toggle');
};
</script>

<style scoped>
/* Subsections using existing variables */
.subsection {
  border-bottom: 1px solid var(--p-surface-200);
}

.subsection:last-child {
  border-bottom: none;
}

.subsection-header {
  background-color: var(--p-surface-50);
  border-bottom: 1px solid var(--p-surface-100);
  font-size: var(--font-size-sm);
  transition: background-color 0.15s ease-in-out;
}

.subsection-header:hover {
  background-color: var(--p-surface-100);
}

.subsection-content {
  background-color: var(--p-surface-0);
}

.chevron-icon {
  font-size: var(--font-size-sm);
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: var(--spacing-md);
  height: var(--spacing-md);
  flex-shrink: 0;
  margin-top: var(--spacing-xxxs);
  margin-left: var(--spacing-md);
}

.flex-1 {
  flex: 1 1 0%;
}
</style>
