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
      :isDataPointToggleable="false"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import { hasDataPointProperValue } from "@/utils/DataPoint";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";

import { HumanizedYesNo } from "@/utils/YesNoNa";

export default defineComponent({
  name: "YesNoExtendedDataPointFormField",
  components: { BaseDataPointFormField, YesNoFormField, ExtendedDataPointFormField },
  inheritAttrs: false,
  data() {
    return {
      HumanizedYesNo,
    }
  },
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
    isDataPointToggleable: {
      type: Boolean,
      default: true,
    },
  },
  emits: ["reportsUpdated"],
  methods: {
    hasDataPointProperValue,
  },
});
</script>