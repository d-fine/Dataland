<template>
  <main id="main-content" role="main">
    <TheAboutContact />
  </main>
</template>

<script setup lang="ts">
import TheAboutContact from '@/components/resources/aboutPage/TheAboutContact.vue';
</script>

<style scoped lang="scss">
main {
  margin-top: 132px;
  @media only screen and (max-width: $bp-md) {
    margin-top: 80px;
  }
}
</style>
