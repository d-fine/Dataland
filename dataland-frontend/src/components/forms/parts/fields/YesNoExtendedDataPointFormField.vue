<template>
  <div class="mb-3" :data-test="name">
    <ExtendedDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :input-class="inputClass"
      :options="HumanizedYesNo"
      :check-value-validity="hasDataPointProperValue"
      :isDataPointToggleable="isDataPointToggleable"
    />
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import { hasDataPointProperValue } from '@/utils/DataPoint';

import { HumanizedYesNo } from '@/utils/YesNoNa';

export default defineComponent({
  name: 'YesNoExtendedDataPointFormField',
  components: { ExtendedDataPointFormField },
  inheritAttrs: false,
  data() {
    return {
      HumanizedYesNo,
    };
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
    isDataPointToggleable: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    hasDataPointProperValue,
  },
});
</script>
