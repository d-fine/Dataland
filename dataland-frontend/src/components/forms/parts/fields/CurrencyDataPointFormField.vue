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
          :v-bind="{ placeholder: customPlaceholder }"
          :validation="validation"
          :unit="unit"
          input-class="col-12"
        />
      </div>
      <div class="col-4">
        <SingleSelectFormElement
          name="currency"
          placeholder="Currency"
          :options="getDataset(DropdownDatasetIdentifier.CurrencyCodes)"
          input-class="long"
        />
      </div>
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { FormFieldPropsWithPlaceholder } from '@/components/forms/parts/fields/FormFieldProps';
import ExtendedDataPointFormField from '@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import { hasDataPointProperValue } from '@/utils/DataPoint';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';

export default defineComponent({
  name: 'CurrencyDataPointFormField',
  computed: {
    DropdownDatasetIdentifier() {
      return DropdownDatasetIdentifier;
    },
  },
  components: {
    SingleSelectFormElement,
    NumberFormField,
    ExtendedDataPointFormField,
    UploadFormHeader,
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
    unit: {
      type: String,
    },
    customPlaceholder: {
      type: String,
      default: 'Absolute Value',
    },
  },
  methods: {
    hasDataPointProperValue,
    getDataset,
  },
});
</script>
