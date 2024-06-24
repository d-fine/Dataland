<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    sub-form-component="EmployeesPerCountryFormElement"
    data-test-add-button="addNewEmployeesPerCountryButton"
    label-add-button="ADD NEW Employees Per Country"
    data-test-sub-form="employeesPerCountrySection"
    data-test-remove-button="removeItemFromListOfEmployeesPerCountry"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';

export default defineComponent({
  name: 'EmployeesPerCountryFormField',
  props: BaseFormFieldProps,
  components: {
    FormListFormField,
  },
});
</script>
