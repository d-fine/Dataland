<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    display-one-sub-form-per-default
    sub-form-component="GeneralViolationsAssessmentFormElement"
    data-test-add-button="addNewGeneralViolationsAssessmentButton"
    label-add-button="ADD NEW General Violation"
    data-test-sub-form="GeneralViolationsAssessmentSection"
    data-test-remove-button="removeItemFromListOfGeneralViolationsAssessments"
  />
</template>

<script lang="ts">
import FormListFormField from "@/components/forms/parts/fields/FormListFormField.vue";
import { defineComponent } from "vue";
export default defineComponent({
  name: "GeneralViolationsAssessmentsFormField",
  components: {
    FormListFormField,
  },
});
</script>
