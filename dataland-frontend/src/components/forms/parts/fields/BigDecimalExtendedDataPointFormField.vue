<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
    :check-value-validity="hasDataPointProperValue"
  >
    <div class="mb-3">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <NumberFormField
        :name="'value'"
        v-model:currentValue="currentValue"
        :validation-label="validationLabel"
        :validation="validation"
        :unit="unit"
        :inner-class="inputClass"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import { hasDataPointProperValue } from "@/utils/DataPoint";

export default defineComponent({
  name: "BigDecimalExtendedDataPointFormField",
  components: { NumberFormField, ExtendedDataPointFormField, UploadFormHeader },
  props: {
    ...BaseFormFieldProps,
    unit: {
      type: String,
    },
  },
  methods: { hasDataPointProperValue },
});
</script>
