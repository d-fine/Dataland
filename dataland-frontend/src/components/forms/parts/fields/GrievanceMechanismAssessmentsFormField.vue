<template>
  <FormListFormField
    :name="name"
    display-one-sub-form-per-default
    sub-form-component="GrievanceMechanismAssessmentFormElement"
    data-test-add-button="addNewGrievanceMechanismAssessmentButton"
    label-add-button="ADD NEW Risk"
    data-test-sub-form="GrievanceMechanismAssessmentSection"
    data-test-remove-button="removeItemFromListOfGrievanceMechanismAssessments"
  />
</template>

<script lang="ts">
// @ts-nocheck
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';
import { defineComponent } from 'vue';
export default defineComponent({
  name: 'GrievanceMechanismAssessmentsFormField',
  components: {
    FormListFormField,
  },
});
</script>
