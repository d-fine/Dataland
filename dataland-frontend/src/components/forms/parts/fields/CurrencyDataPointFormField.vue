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
    <div class="grid">
      <div class="col-12">
        <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      </div>
      <div class="col-4">
        <NumberFormField
          :name="'value'"
          :validation-label="validationLabel"
          :validation="validation"
          :unit="unit"
          input-class="col-12"
        />
      </div>
      <div class="col-4">
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
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import { hasDataPointProperValue } from "@/utils/DataPoint";

export default defineComponent({
  name: "CurrencyDataPointFormField",
  computed: {
    DropdownDatasetIdentifier() {
      return DropdownDatasetIdentifier;
    },
  },
  components: { NumberFormField, ExtendedDataPointFormField, UploadFormHeader, FormKit },
  props: {
    ...BaseFormFieldProps,
    unit: {
      type: String,
    },
  },
  methods: {
    hasDataPointProperValue,
    getDataset,
  },
});
</script>
