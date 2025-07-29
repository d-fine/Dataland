<template>
  <i
    class="pi pi-spinner pi-spin"
    aria-hidden="true"
    :style="{
      color: color || 'var(--p-primary-color)',
      fontSize: fontSize || '1rem',
      zIndex: 20,
    }"
  />
</template>

<script setup lang="ts">
const { color, fontSize } = defineProps({
  color: {
    type: String,
    required: false,
  },
  fontSize: {
    type: String,
    required: false,
  },
});
</script>
