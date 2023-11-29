<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
    :check-value-validity="hasDataPointProperValue"
    :shouldBeToggle="shouldBeToggle"
  >
    <div class="mb-2">
      <NumberFormField
        :name="'value'"
        :validation-label="validationLabel"
        :validation="shouldBeToggle || required ? 'required' : ''"
        :unit="unit"
        :input-class="inputClass"
      />
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import { hasDataPointProperValue } from "@/utils/DataPoint";

export default defineComponent({
  name: "BigDecimalExtendedDataPointFormField",
  components: { NumberFormField, ExtendedDataPointFormField },
  props: {
    ...BaseFormFieldProps,
    unit: {
      type: String,
    },
    shouldBeToggle: {
      type: Boolean,
      default: true,
    },
  },
  methods: { hasDataPointProperValue },
});
</script>
