<template>
  <div data-test="submitButton" class="text-center p-3">
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
  name: "SubmitButton",
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
