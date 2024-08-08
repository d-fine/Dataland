<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :validation-label="validationLabel"
    :validation="validation"
    :input-class="inputClass"
    :check-value-validity="hasDataPointProperValue"
    :isDataPointToggleable="isDataPointToggleable"
    :placeholder="placeholder"
    :outerClass="inputClass"
  >
    <div class="mb-2">
      <PercentageFormField
        :name="'value'"
        :validation-label="validationLabel"
        :validation="validation"
        :input-class="inputClass"
        :placeholder="placeholder"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
// @ts-nocheck
import {defineComponent} from 'vue';
import {BaseFormFieldProps} from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import {hasDataPointProperValue} from '@/utils/DataPoint';

export default defineComponent({
  name: 'PercentageExtendedDataPointFormField',
  components: { PercentageFormField, ExtendedDataPointFormField },
  props: {
    ...BaseFormFieldProps,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: String,
      default: 'Value in %',
    },
  },
  methods: { hasDataPointProperValue },
});
</script>
