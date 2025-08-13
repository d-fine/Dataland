<template>
  <div class="content-box" :class="themeClasses">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ContentTheme } from './types';

interface Props {
  theme: ContentTheme;
}

const props = defineProps<Props>();

const themeClasses = computed(() => {
  return `content-box--${props.theme}`;
});
</script>

<style scoped>
.content-box {
  padding: var(--spacing-sm);
  border-radius: var(--p-border-radius-xs);
  border-left: var(--spacing-xxxs) solid;
  font-size: var(--font-size-sm);
}

/* Blue theme */
.content-box--blue {
  background-color: var(--p-blue-50);
  border-left-color: var(--p-blue-400);
  color: var(--p-blue-800);
}

.content-box--blue :deep(code) {
  background-color: var(--p-blue-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--blue :deep(a) {
  color: var(--p-blue-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

/* Cyan theme */
.content-box--cyan {
  background-color: var(--p-cyan-50);
  border-left-color: var(--p-cyan-400);
  color: var(--p-cyan-800);
}

.content-box--cyan :deep(code) {
  background-color: var(--p-cyan-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--cyan :deep(a) {
  color: var(--p-cyan-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

/* Green theme */
.content-box--green {
  background-color: var(--p-green-50);
  border-left-color: var(--p-green-400);
  color: var(--p-green-800);
}

.content-box--green :deep(code) {
  background-color: var(--p-green-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--green :deep(a) {
  color: var(--p-green-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

/* Orange theme */
.content-box--orange {
  background-color: var(--p-orange-50);
  border-left-color: var(--p-orange-400);
  color: var(--p-orange-800);
}

.content-box--orange :deep(code) {
  background-color: var(--p-orange-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--orange :deep(a) {
  color: var(--p-orange-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

/* Purple theme */
.content-box--purple {
  background-color: var(--p-purple-50);
  border-left-color: var(--p-purple-400);
  color: var(--p-purple-800);
}

.content-box--purple :deep(code) {
  background-color: var(--p-purple-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--purple :deep(a) {
  color: var(--p-purple-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

/* Indigo theme */
.content-box--indigo {
  background-color: var(--p-indigo-50);
  border-left-color: var(--p-indigo-400);
  color: var(--p-indigo-800);
}

.content-box--indigo :deep(code) {
  background-color: var(--p-indigo-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--indigo :deep(a) {
  color: var(--p-indigo-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}

/* Teal theme */
.content-box--teal {
  background-color: var(--p-teal-50);
  border-left-color: var(--p-teal-400);
  color: var(--p-teal-800);
}

.content-box--teal :deep(code) {
  background-color: var(--p-teal-100);
  padding: var(--spacing-xxxs) var(--spacing-xs);
  border-radius: var(--p-border-radius-xs);
  font-family: monospace;
  margin-left: var(--spacing-xs);
}

.content-box--teal :deep(a) {
  color: var(--p-teal-700);
  margin-left: var(--spacing-xxxs);
  transition: color 0.15s ease-in-out;
  word-break: break-all;
}
</style>
