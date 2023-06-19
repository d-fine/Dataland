<template>
  <router-link to="/requests" class="no-underline" data-test="requestDataButton">
    <PrimeButton class="uppercase p-button p-button-sm mr-3">
      <i class="material-icons"> add_box </i>
      <span class="d-letters pl-2"> Request Data </span>
    </PrimeButton>
  </router-link>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
export default defineComponent({
  name: "RequestDataButton",
  components: { PrimeButton },
});
</script>
