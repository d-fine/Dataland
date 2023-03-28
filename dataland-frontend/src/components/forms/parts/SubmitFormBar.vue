<template>
  <div class="d-header fixed bottom-0 left-0 right-0 m-0 p-1 pr-3 w-full surface-900 grid justify-content-end">
    <PrimeButton data-test="submitButton" type="submit" :label="label" :class="dynamicClasses" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "SubmitFormBar",
  components: {
    PrimeButton,
  },
  props: {
    label: {
      type: String,
      default: "SUBMIT FORM",
    },
  },
  computed: {
    dynamicClasses() {
      const formIsInvalid = !this.$formkit.get("createLkSGForm")?.context?.state.valid;
      if (formIsInvalid) {
        return "button-disabled";
      } else {
        return "";
      }
    },
  },
});
</script>
