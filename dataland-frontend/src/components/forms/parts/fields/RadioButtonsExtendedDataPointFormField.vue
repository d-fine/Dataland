<template>
  <div class="mb-3" :data-test="name">
    <ExtendedDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :input-class="inputClass"
      :options="options"
      :isDataPointToggleable="isDataPointToggleable"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { OptionsFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';

export default defineComponent({
  name: 'RadioButtonsExtendedDataPointFormField',
  components: { ExtendedDataPointFormField },
  props: {
    ...OptionsFormFieldProps,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
