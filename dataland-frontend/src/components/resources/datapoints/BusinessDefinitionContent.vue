<template>
  <p>
    {{ businessDefinition }}
  </p>
</template>

<script setup lang="ts">
interface Props {
  businessDefinition: string;
}

defineProps<Props>();
</script>
