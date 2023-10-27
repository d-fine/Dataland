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
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div class="next-to-each-other">
        <FormKit
          type="text"
          name="value"
          v-model="currentValue"
          :validation-label="validationLabel ?? label"
          :validation="`number|${validation}`"
          :placeholder="unit ? `Value in ${unit}` : 'Value'"
          outer-class="short"
          @blur="handleBlurValue"
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
import { FormKit } from "@formkit/vue";
import { QualityOptions } from "@clients/backend";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";

export default defineComponent({
  name: "BigDecimalExtendedDataPointFormField",
  components: { ExtendedDataPointFormField, UploadFormHeader, FormKit },
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
  methods: {
    /**
     * Handle blur event on value input.
     */
    handleBlurValue() {
      const extendedDataPointFormField = this.$refs.extendedDataPointFormField;
      if (this.currentValue === "") {
        extendedDataPointFormField.setQuality(QualityOptions.Na);
      } else if (this.currentValue !== "" && extendedDataPointFormField.isQualityNa()) {
        extendedDataPointFormField.setQuality(undefined);
      }
    },
  },
});
</script>
