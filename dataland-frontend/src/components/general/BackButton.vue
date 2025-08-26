<template>
  <Button
    icon="pi pi-chevron-left"
    variant="text"
    @click="router.back()"
    data-test="back-button"
    label="BACK"
    :pt="{
      root: {
        style: 'padding-left: 0',
      },
      label: {
        style: 'font-weight: var(--font-weight-semibold)',
      },
      icon: {
        style: 'font-size: var(--font-size-lg);',
      },
    }"
  />
</template>

<script setup lang="ts">
import router from '@/router';
import Button from 'primevue/button';
</script>
