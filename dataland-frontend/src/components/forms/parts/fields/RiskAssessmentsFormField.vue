<template>
  <FormListFormField
    sub-form-component="RiskAssessmentFormElement"
    data-test-add-button="addNewRiskAssessmentButton"
    label-add-button="ADD NEW Risk"
    data-test-sub-form="RiskAssessmentSection"
    data-test-remove-button="removeItemFromListOfRiskAssessments"
  />
</template>

<script lang="ts">
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'RiskAssessmentsFormField',
  components: {
    FormListFormField,
  },
});
</script>
