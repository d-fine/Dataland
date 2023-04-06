<template>
  <div
    data-test="submitFormBar"
    class="sticky d-header fixed bottom-0 left-0 right-0 m-0 p-1 pr-3 w-full surface-900 grid justify-content-end"
  >
    <PrimeButton
      data-test="submitButton"
      type="submit"
      :label="updatingData ? 'UPDATE DATA' : 'ADD DATA'"
      :class="formIsValid ? '' : 'button-disabled'"
      @click="submit"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import { useRoute } from "vue-router";
import { assertDefined } from "@/utils/TypeScriptUtils";

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
    formId: {
      type: String,
      required: true,
    },
  },
  computed: {
    updatingData(): boolean {
      return this.route.query.templateDataId !== undefined;
    },
    formIsValid(): boolean {
      return assertDefined(this.$formkit.get(this.formId)?.context?.state.valid);
    },
  },
  methods: {
    /**
     * Submits the form associated with the provided form ID
     */
    submit() {
      this.$formkit.submit(this.formId);
    },
  },
});
</script>

<style scoped>
.sticky {
  position: -webkit-sticky;
  position: sticky;
  bottom: 0;
}
</style>
