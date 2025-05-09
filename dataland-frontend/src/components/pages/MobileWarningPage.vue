<template>
  <TheHeader />
  <div class="surface-ground pb-8">
    <h2 class="font-light d-letters text-4xl m-6 text-left">
      We are working on making the website display properly on mobile. <br />
      For now we invite you to use the desktop version.
    </h2>
  </div>
  <TheFooter />
</template>

<script setup lang="ts">
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
</script>
