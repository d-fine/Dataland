<template>
  <PrimeButton class="uppercase p-button-outlined p-button-sm mr-3">
    <i class="material-icons"> clear </i>
    <span class="d-letters pl-2"> Reject </span>
  </PrimeButton>
  <PrimeButton class="uppercase p-button p-button-sm">
    <i class="material-icons"> done </i>
    <span class="d-letters pl-2"> Approve </span>
  </PrimeButton>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
export default defineComponent({
  name: "QualityAssuranceButtons",
  components: { PrimeButton },
});
</script>
