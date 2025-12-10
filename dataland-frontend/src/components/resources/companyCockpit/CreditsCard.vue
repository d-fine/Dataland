<template>
  <Card
    style="width: 70%; margin: 0 auto; margin-top: var(--spacing-xl); margin-bottom: var(--spacing-xl)"
    data-test="creditsBalance"
  >
    <template #title>Credits</template>
    <template #content>
      <Divider />
      Current Amount of Credits:
      <div class="dataland-info-text small">{{ props.companyId }}</div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import { defineProps } from 'vue';

const props = defineProps<{
  companyId: string;
}>();
</script>

<style scoped></style>
