<template>
  <div
    data-test="submitFormBar"
    class="d-header fixed bottom-0 left-0 right-0 m-0 p-1 pr-3 w-full surface-900 grid justify-content-end"
  >
    <PrimeButton
      data-test="submitButton"
      type="submit"
      :label="updatingData ? 'UPDATE DATA' : 'ADD DATA'"
      :class="formIsValid ? '' : 'button-disabled'"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import { useRoute } from "vue-router";

export default defineComponent({
  name: "SubmitFormBar",
  components: {
    PrimeButton,
  },
  data() {
    return {
      route: useRoute(),
    };
  },
  props: {
    formIsValid: {
      type: Boolean,
      required: true,
    },
  },
  computed: {
    updatingData(): boolean {
      return this.route.query.templateDataId !== undefined;
    },
  },
});
</script>
