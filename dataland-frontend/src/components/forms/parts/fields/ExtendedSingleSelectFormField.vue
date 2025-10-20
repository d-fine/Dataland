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
      <SingleSelectFormElement
        name="value"
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
import { defineComponent } from 'vue';
import { BaseFormFieldProps, DropdownOptionFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'ExtendedSingleSelectFormField',
  components: { SingleSelectFormElement, ExtendedDataPointFormField },
  props: {
    ...BaseFormFieldProps,
    ...DropdownOptionFormFieldProps,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
