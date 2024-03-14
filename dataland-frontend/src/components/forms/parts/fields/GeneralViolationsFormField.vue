<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    display-one-sub-form-per-default
    sub-form-component="GeneralViolationFormElement"
    data-test-add-button="addNewGeneralViolationButton"
    label-add-button="ADD NEW Violation"
    data-test-sub-form="GeneralViolationSection"
    data-test-remove-button="removeItemFromListOfGeneralViolations"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import FormListFormField from "@/components/forms/parts/fields/FormListFormField.vue";

export default defineComponent({
  name: "GeneralViolationsFormField",
  components: { FormListFormField },
});
</script>
