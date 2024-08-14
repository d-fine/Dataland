<template>
  <ExtendedDataPointFormField
    ref="extendedDataPointFormField"
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    :input-class="inputClass"
    :check-value-validity="hasDataPointProperValue"
    :placeholder="placeholder"
    :validation="validation"
    :validation-label="validationLabel"
  >
    <div class="grid">
      <div class="col-4">
        <NumberFormField
          :name="'value'"
          :placeholder="placeholder"
          :validation="validation"
          :validation-label="validationLabel"
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
          data-test="currency"
        />
      </div>
    </div>
  </ExtendedDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
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
  },
  props: {
    ...FormFieldPropsWithPlaceholder,
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
