<template>
  <router-link to="/singledatarequest" class="no-underline" data-test="singleDataRequestButton">
    <PrimeButton class="uppercase p-button p-button-sm mr-3">
      <span class="d-letters pl-2"> Request Data </span>
    </PrimeButton>
  </router-link>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "SingleDataRequestButton",
  components: { PrimeButton },
});
</script>
