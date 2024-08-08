<template>
  <ExtendedDataPointFormField
      ref="extendedDataPointFormField"
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :validation-label="validationLabel"
      :validation="validation"
      :check-value-validity="hasDataPointProperValue"
      :isDataPointToggleable="isDataPointToggleable"
      :input-class="inputClass"
  >
    <div class="form-field">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <CheckboxesListFormElement
          :name="name"
          :validation="validation"
          :validation-label="validationLabel ?? label"
          :options="options"
          :field-name="name"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
// @ts-nocheck
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import {defineComponent} from 'vue';
import CheckboxesListFormElement from '@/components/forms/parts/elements/basic/CheckboxesListFormElement.vue';
import {OptionsFormFieldProps} from '@/components/forms/parts/fields/FormFieldProps';
import {hasDataPointProperValue} from "@/utils/DataPoint";
import ExtendedDataPointFormField from '../elements/basic/ExtendedDataPointFormField.vue';

export default defineComponent({
  name: 'RadioButtonsExtendedDataPointFormField',
  components: { CheckboxesListFormElement, UploadFormHeader, ExtendedDataPointFormField },
  props: {
    ...OptionsFormFieldProps,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
  },
  methods: { hasDataPointProperValue },
});
</script>
