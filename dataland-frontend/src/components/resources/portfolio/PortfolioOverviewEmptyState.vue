<template>
  <div v-if="isPending" class="text-center px-7 py-4" data-test="portfolios-loading">
    <h1>Loading portfolios...</h1>
    <DatalandProgressSpinner />
  </div>
  <div v-else-if="isError" class="text-center px-7 py-4" data-test="portfolios-error">
    <h1>Error loading portfolios</h1>
    An unexpected error occurred. Please try again later or with [Ctrl] + [F5] or contact the support team if the issue
    persists.
  </div>
  <h1 v-else>No Portfolios available.</h1>
</template>

<script setup lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';

defineProps<{
  isPending: boolean;
  isError: boolean;
}>();
</script>
