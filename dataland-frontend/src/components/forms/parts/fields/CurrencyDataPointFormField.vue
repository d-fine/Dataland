<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :inner-class="innerClass"
    :check-value-validity="hasDataPointProperValue"
  >
    <div class="mb-3">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div class="next-to-each-other">
        <NumberFormField :name="'value'" :validation-label="validationLabel" :validation="validation" :unit="unit" />
        <div class="mt-3">
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
  },
  methods: {
    hasDataPointProperValue,
    getDataset,
  },
});
</script>
