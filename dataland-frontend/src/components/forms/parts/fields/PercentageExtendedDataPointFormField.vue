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
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';

export default defineComponent({
  name: 'PercentageExtendedDataPointFormField',
  components: { PercentageFormField, ExtendedDataPointFormField },
  props: {
    ...BaseFormFieldProps,
    placeholder: {
      type: String,
      default: 'Value in %',
    },
  },
});
</script>
