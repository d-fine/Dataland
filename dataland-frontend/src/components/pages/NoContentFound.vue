<template>
  <TheContent>
    <h2>Sorry an error occurred!</h2>
    <p>Whoops! Something went wrong!</p>
    <p>
      Go back to
      <a href="/" class="text-primary" title="back to landing page">Dataland</a>
    </p>
  </TheContent>
</template>

<script lang="ts">
import TheContent from '@/components/generics/TheContent.vue';

export default {
  name: 'NoContentFound',
  components: { TheContent },
};
</script>
<style scoped>
.text-primary {
  color: var(--p-primary-color);
}
</style>
