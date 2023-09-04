<template>
  <footer>
    <p :class="[!isMobile ? ['font-medium', 'text-xl'] : ['uppercase', 'font-light', 'd-letters', 'text-3xl']]">
      footer
    </p>
    <router-link to="/imprint" class="no-underline" data-test="imprint">
      <span class="font-semibold underline text-primary cursor-pointer" title="imprint">Imprint</span>
    </router-link>
    <router-link to="/dataprivacy" class="no-underline" data-test="data privacy">
      <p class="font-semibold underline text-primary cursor-pointer" title="data privacy">Data Privacy</p>
    </router-link>

    <p>Copyright &#169; 2023 Dataland</p>
  </footer>
</template>

<script setup lang="ts">
const { isMobile } = defineProps<{
  isMobile: boolean;
}>();
</script>

<style scoped lang="scss">
.footer {
  padding: 8vw;
  align-content: space-between;
  .legal {
    margin-left: auto;
  }
}
</style>
