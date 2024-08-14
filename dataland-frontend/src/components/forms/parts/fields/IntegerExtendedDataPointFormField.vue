<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
  >
    <div class="mb-3">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <NumberFormField
        :name="'value'"
        :validation-label="validationLabel ?? label"
        :validation="`integer|${validation}`"
        :unit="unit"
        :placeholder="unit ? `Value in ${unit}` : 'Value'"
        :input-class="inputClass"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';

export default defineComponent({
  name: 'IntegerExtendedDataPointFormField',
  components: { NumberFormField, ExtendedDataPointFormField, UploadFormHeader },
  props: {
    ...FormFieldPropsWithPlaceholder,
    unit: {
      type: String,
    },
  },
});
</script>
