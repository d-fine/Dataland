<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
    :isDataPointToggleable="isDataPointToggleable"
  >
    <div :class="containerClass">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <SingleSelectFormElement
        :name="name"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :placeholder="placeholder"
        :options="options"
        :input-class="inputClass"
        :required="required"
        :class="containerClass"
        :ignore="ignore"
        :data-test="dataTest"
        :validation-messages="validationMessages"
        :deselect-removed-options-on-shrinkage="deselectRemovedOptionsOnShrinkage"
        :allow-unknown-option="allowUnknownOption"
        v-bind:model-value="selectedOption"
        @update:model-value="handleInputChange"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent, type PropType } from 'vue';
import { BaseFormFieldProps, DropdownOptionFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'BigDecimalExtendedDataPointFormField',
  components: { SingleSelectFormElement, UploadFormHeader, ExtendedDataPointFormField },
  props: {
    ...BaseFormFieldProps,
    ...DropdownOptionFormFieldProps,
    inputClass: { type: String, default: 'long' },
    containerClass: { type: String, default: 'form-field' },
    ignore: { type: Boolean, default: false },
    deselectRemovedOptionsOnShrinkage: {
      type: Boolean,
      default: true,
    },
    allowUnknownOption: {
      type: Boolean,
      default: false,
    },
    modelValue: {
      type: String as PropType<string | null>,
    },
    dataTest: String,
    validationMessages: Object,
    isDataPointToggleable: {
      type: Boolean,
      default: true,
    },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      selectedOption: null as null | string,
    };
  },
  watch: {
    modelValue(newValue: string) {
      this.selectedOption = newValue;
    },
  },
  methods: {
    /**
     * Handler for changes in the dropdown component
     * @param newInput the new value in the dropdown
     */
    handleInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit('update:modelValue', this.selectedOption);
    },
  },
});
</script>
