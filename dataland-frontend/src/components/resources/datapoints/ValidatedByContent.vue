<template>
  <ContentBox theme="green">
    <strong>Validation Class:</strong>
    <code>{{ validatedBy }}</code>
  </ContentBox>
</template>

<script setup lang="ts">
import ContentBox from './ContentBox.vue';

interface Props {
  validatedBy: string;
}

defineProps<Props>();
</script>
