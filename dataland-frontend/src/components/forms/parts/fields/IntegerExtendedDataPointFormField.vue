<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :inner-class="innerClass"
    :is-data-value-provided="currentValue != '' && currentValue != undefined"
  >
    <div class="mb-3">
      <UploadFormHeader :label="label" :description="description" :is-required="required" />
      <div class="next-to-each-other">
        <NumberFormField
          :name="'value'"
          v-model:currentValue="currentValue"
          :validation-label="validationLabel ?? label"
          :validation="`integer|${validation}`"
          :placeholder="unit ? `Value in ${unit}` : 'Value'"
        />
        <div v-if="unit" class="form-field-label pb-3">
          <span>{{ unit }}</span>
        </div>
      </div>
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";

export default defineComponent({
  name: "IntegerExtendedDataPointFormField",
  components: { NumberFormField, ExtendedDataPointFormField, UploadFormHeader },
  data() {
    return {
      currentValue: "",
    };
  },
  props: {
    ...BaseFormFieldProps,
    unit: {
      type: String,
    },
  },
});
</script>
