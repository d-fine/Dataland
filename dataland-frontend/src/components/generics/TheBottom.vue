<template>
  <div class="d-header m-0 fixed bottom-0 surface-900 h-4rem w-full grid align-items-center">
    <div class="col-1 col-offset-10">
      <PrimeButton
        label="Reset"
        class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center surface-900 w-6rem"
        name="reset_request_button"
      >
        Reset
      </PrimeButton>
    </div>
    <div class="col-1">
      <PrimeButton
        label="Submit"
        class="uppercase p-button p-button-sm d-letters text-white d-button justify-content-center bg-primary w-6rem mr-9"
        name="submit_request_button"
      />
    </div>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
export default {
  name: "TheBottom",
  components: { PrimeButton },
};
</script>
