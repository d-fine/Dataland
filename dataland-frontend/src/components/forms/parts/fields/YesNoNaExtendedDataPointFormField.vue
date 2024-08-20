<template>
  <div class="mb-3" :data-test="name">
    <ExtendedDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :input-class="inputClass"
      :options="HumanizedYesNoNa"
      :isDataPointToggleable="isDataPointToggleable"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';

import { HumanizedYesNoNa } from '@/utils/YesNoNa';

export default defineComponent({
  name: 'YesNoNaExtendedDataPointFormField',
  components: { ExtendedDataPointFormField },
  inheritAttrs: false,
  data() {
    return {
      HumanizedYesNoNa,
    };
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
    isDataPointToggleable: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
