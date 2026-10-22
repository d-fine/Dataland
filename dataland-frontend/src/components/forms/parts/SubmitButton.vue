<template>
  <div data-test="submitButton" class="text-left">
    <PrimeButton
      data-test="submitButton"
      type="submit"
      :label="'ADD DATA'"
      :class="formIsValid ? 'button-enabled col-12 m-0' : 'button-disabled col-12 m-0'"
      @click="submit"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { submitForm, getNode } from '@formkit/core';

export default defineComponent({
  name: 'SubmitButton',
  components: {
    PrimeButton,
  },
  props: {
    formId: {
      type: String,
      required: true,
    },
  },
  computed: {
    formIsValid(): boolean {
      return assertDefined(getNode(this.formId)?.context?.state.valid);
    },
  },
  methods: {
    /**
     * Submits the form associated with the provided form ID
     */
    submit() {
      submitForm(this.formId);
    },
  },
});
</script>

<style scoped>
.button-enabled {
  outline: var(--spacing-lg);
  outline-offset: var(--spacing-lg);
}

.button-disabled {
  opacity: 0.4;
  --p-button-primary-border-color: transparent;
  --p-button-primary-hover-background: var(--p-button-primary-background);
  --p-button-primary-active-background: var(--p-button-primary-background);
  --p-button-primary-hover-border-color: transparent;
  --p-button-primary-active-border-color: transparent;
  --p-button-primary-hover-color: var(--p-button-primary-color);
  --p-button-primary-active-color: var(--p-button-primary-color);
}
</style>
