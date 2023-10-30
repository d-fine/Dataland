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
        <NumberFormField
          :name="'value'"
          v-model="currentValue"
          :validation-label="validationLabel ?? label"
          :validation="`number|integer|${validation}`"
          :placeholder="unit ? `Value in ${unit}` : 'Value'"
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
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";

export default defineComponent({
  name: "IntegerExtendedDataPointFormField",
  components: {NumberFormField, ExtendedDataPointFormField, UploadFormHeader, FormKit },
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
      const setQuality = extendedDataPointFormField.setQuality as (quality?: QualityOptions) => void;
      const isQualityNa = extendedDataPointFormField.isQualityNa as () => boolean;
      if (this.currentValue === "") {
        setQuality(QualityOptions.Na);
      } else if (this.currentValue !== "" && isQualityNa()) {
        setQuality(undefined);
      }
    },
  },
});
</script>
