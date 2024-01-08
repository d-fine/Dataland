<template>
  <GdvYearlyDecimalTimeseriesDataFormElement
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
import { defineComponent } from "vue";
import GdvYearlyDecimalTimeseriesDataFormElement from "@/components/forms/parts/elements/derived/GdvYearlyDecimalTimeseriesDataFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "GdvYearlyDecimalTimeseriesThreeYearDeltaDataFormField",
  components: { GdvYearlyDecimalTimeseriesDataFormElement },
  props: {
    ...DropdownOptionFormFieldProps,
    reportingPeriod: {
      type: String,
      required: false,
    },
  },
});
</script>
