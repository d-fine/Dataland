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
          :validation="`number|integer|${validation}`"
          :validationMessages="{ integer: `${validationLabel ?? label} must be an integer.` }"
          :validationRules="{ integer }"
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
import { type FormKitNode } from "@formkit/core";

export default defineComponent({
  name: "IntegerExtendedDataPointFormField",
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
     * Checks if a node has an integer velue
     * @param node Node whose value to check for being an integer
     * @returns true iff the provided node value is an integer
     */
    integer(node: FormKitNode): boolean {
      const fieldValue = node.value as string;
      return !isNaN(parseInt(fieldValue)) && parseInt(fieldValue) == parseFloat(fieldValue);
    },
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
