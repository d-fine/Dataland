<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :validation="validation"
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
          placeholder="Value"
          outer-class="short"
          @blur="handleBlurValue"
        />
        <FormKit
          type="select"
          name="currency"
          placeholder="Currency"
          :options="getDataset(DropdownDatasetIdentifier.CurrencyCodes)"
          outer-class="short"
          data-test="datapoint-currency"
        />
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
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "BigDecimalExtendedDataPointFormField",
  computed: {
    DropdownDatasetIdentifier() {
      return DropdownDatasetIdentifier;
    },
  },
  components: { ExtendedDataPointFormField, UploadFormHeader, FormKit },
  data() {
    return {
      currentValue: "",
    };
  },
  props: {
    ...BaseFormFieldProps,
    options: {
      type: Array,
    },
  },
  methods: {
    getDataset,
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
