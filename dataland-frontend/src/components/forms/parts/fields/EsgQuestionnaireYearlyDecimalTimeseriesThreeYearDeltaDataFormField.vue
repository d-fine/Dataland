<template>
  <EsgQuestionnaireYearlyDecimalTimeseriesDataFormElement
    :name="name"
    :description="description"
    :label="label"
    :validation="validation"
    :validation-label="validationLabel"
    :required="required"
    :input-class="inputClass"
    :options="options"
    :reporting-period="reportingPeriod"
    :n-years-into-future="3"
    :n-years-into-past="3"
    :show-current-year="true"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import EsgQuestionnaireYearlyDecimalTimeseriesDataFormElement from '@/components/forms/parts/elements/derived/EsgQuestionnaireYearlyDecimalTimeseriesDataFormElement.vue';
import { DropdownOptionFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'EsgQuestionnaireYearlyDecimalTimeseriesThreeYearDeltaDataFormField',
  components: { EsgQuestionnaireYearlyDecimalTimeseriesDataFormElement },
  props: {
    ...DropdownOptionFormFieldProps,
    reportingPeriod: {
      type: String,
      required: false,
    },
  },
});
</script>
