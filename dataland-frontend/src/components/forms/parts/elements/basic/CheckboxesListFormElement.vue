<template>
  <FormKit
    type="checkbox"
    :key="key"
    :name="name"
    v-model="checkboxValue"
    :options="options"
    :outer-class="{
      'yes-no-radio': true,
    }"
    :inner-class="{
      'formkit-inner': false,
    }"
    :input-class="{
      'formkit-input': false,
      'p-radiobutton': true,
    }"
    :id="fieldName + name"
    :ignore="true"
    :plugins="[disabledOnMoreThanOne]"
    @input="handleCheckboxSelectionByUser($event)"
  />
  <FormKit
    type="text"
    :name="name"
    v-model="currentValue"
    :validation="validation"
    :validation-label="validationLabel"
    :validation-messages="validationMessages"
    v-if="!shouldBeIgnoredByFormKit"
    :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
  />
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { FormKit } from '@formkit/vue';
import { disabledOnMoreThanOne } from '@/utils/FormKitPlugins';

export default defineComponent({
  name: 'CheckboxesListFormElement',
  components: { FormKit },
  data() {
    return {
      key: 0,
      shouldBeIgnoredByFormKit: false,
      currentValue: null as string | null,
      checkboxValue: [] as Array<string>,
    };
  },
  methods: {
    disabledOnMoreThanOne,
    /**
     * Handles the input event in the checkboxes
     * @param newCheckboxValue is the selected value in the checkbox that triggered this @input event
     */
    handleCheckboxSelectionByUser(newCheckboxValue: [string]) {
      const newCheckboxValueAsString = newCheckboxValue[0];
      this.updateCurrentValue(newCheckboxValueAsString);
    },
    /**
     * Updates the currentValue when the checkboxes value has been changed
     * @param newCheckBoxValue is the new value in the checkbox
     */
    updateCurrentValue(newCheckBoxValue: string) {
      if (newCheckBoxValue && newCheckBoxValue !== '') {
        this.shouldBeIgnoredByFormKit = false;
        this.currentValue = newCheckBoxValue;
        this.checkboxValue = [newCheckBoxValue];
      } else {
        this.shouldBeIgnoredByFormKit = !this.validation.includes('is:');
        this.currentValue = null;
      }
    },
  },
  watch: {
    currentValue(newVal: string) {
      this.updateCurrentValue(newVal);
      this.$emit('updateCheckboxValue', this.currentValue);
    },
  },
  emits: ['updateCheckboxValue'],
  props: {
    name: {
      type: String,
      default: '',
    },
    options: {
      type: Object,
      required: true,
    },
    validation: {
      type: String,
      default: '',
    },
    validationLabel: {
      type: String,
      default: '',
    },
    validationMessages: {
      type: Object as () => { is: string },
    },
    fieldName: {
      type: String,
      default: '',
    },
  },
});
</script>
