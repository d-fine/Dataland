<template>
  <router-link to="/companies/choose" class="no-underline" data-test="newDatasetButton">
    <PrimeButton class="uppercase p-button p-button-sm mr-3">
      <i class="material-icons"> library_add </i>
      <span class="d-letters pl-2"> New Dataset </span>
    </PrimeButton>
  </router-link>
</template>

<script lang="ts">
import PrimeButton from 'primevue/button';
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'NewDatasetButton',
  components: { PrimeButton },
});
</script>
