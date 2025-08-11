<template>
  <TheHeader :showUserProfileDropdown="false" />
  fgfghfghfghfg
  <TheFooter />
</template>

<script lang="ts">
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';

export default {
  name: 'NoContentFound',
  components: { TheHeader, TheFooter },
};
</script>
