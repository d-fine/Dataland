<template>
  <router-link to="/bulk-data-request" class="no-underline" data-test="bulkDataRequestButton">
    <PrimeButton class="uppercase p-button p-button-sm mr-3">
      <i class="material-icons"> add_box </i>
      <span class="d-letters pl-2"> Bulk Data Request </span>
    </PrimeButton>
  </router-link>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
export default defineComponent({
  name: "RequestBulkDataButton",
  components: { PrimeButton },
});
</script>
