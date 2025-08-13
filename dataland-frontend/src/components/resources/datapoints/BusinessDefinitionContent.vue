<template>
  <p style="line-height: 1.625; color: var(--p-surface-700)">
    {{ businessDefinition }}
  </p>
</template>

<script setup lang="ts">
interface Props {
  businessDefinition: string;
}

defineProps<Props>();
</script>
