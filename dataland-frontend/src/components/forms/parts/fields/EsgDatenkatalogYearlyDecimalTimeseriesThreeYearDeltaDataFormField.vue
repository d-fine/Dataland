<template>
  <EsgDatenkatalogYearlyDecimalTimeseriesDataFormElement
    :name="name"
    :description="description"
    :label="label"
    :validation="validation"
    :validationLabel="validationLabel"
    :required="required"
    :inputClass="inputClass"
    :options="options"
    :reportingPeriod="reportingPeriod"
    :nYearsIntoFuture="3"
    :nYearsIntoPast="3"
    :showCurrentYear="true"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import EsgDatenkatalogYearlyDecimalTimeseriesDataFormElement from '@/components/forms/parts/elements/derived/EsgDatenkatalogYearlyDecimalTimeseriesDataFormElement.vue';
import { DropdownOptionFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'EsgDatenkatalogYearlyDecimalTimeseriesThreeYearDeltaDataFormField',
  components: { EsgDatenkatalogYearlyDecimalTimeseriesDataFormElement },
  props: {
    ...DropdownOptionFormFieldProps,
    reportingPeriod: {
      type: String,
      required: false,
    },
  },
});
</script>
