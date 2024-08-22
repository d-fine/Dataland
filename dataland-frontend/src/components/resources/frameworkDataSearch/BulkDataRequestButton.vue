<template>
  <router-link to="/bulkdatarequest" class="no-underline" data-test="bulkDataRequestButton">
    <PrimeButton class="uppercase p-button p-button-sm mr-3">
      <i class="material-icons"> add_box </i>
      <span class="d-letters pl-2"> BULK DATA REQUEST </span>
    </PrimeButton>
  </router-link>
</template>

<script lang="ts">
import PrimeButton from 'primevue/button';
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'BulkDataRequestButton',
  components: { PrimeButton },
});
</script>
