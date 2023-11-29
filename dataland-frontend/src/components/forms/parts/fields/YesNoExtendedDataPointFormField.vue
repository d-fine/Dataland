<template>
  <div class="mb-3" :data-test="name">
    <ExtendedDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :input-class="inputClass"
      :check-value-validity="hasDataPointProperValue"
      :show-data-point-fields-without-value="false"
    >
      <YesNoFormField
        name="value"
        :label="label"
        :description="description"
        :is-required="required"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :radio-buttons-data-test="dataTest"
        classes=""
      />
    </ExtendedDataPointFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import { hasDataPointProperValue } from "@/utils/DataPoint";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";

export default defineComponent({
  name: "YesNoExtendedDataPointFormField",
  components: { YesNoFormField, ExtendedDataPointFormField },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
  },
  emits: ["reportsUpdated"],
  methods: {
    hasDataPointProperValue,
  },
});
</script>
