<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    sub-form-component="SubsidiaryFormElement"
    data-test-add-button="addNewSubsidiaryButton"
    label-add-button="ADD NEW Subsidiary"
    data-test-sub-form="subsidiarySection"
    data-test-remove-button="removeItemFromListOfSubsidiary"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';

export default defineComponent({
  name: 'SubsidiaryFormField',
  props: BaseFormFieldProps,
  components: {
    FormListFormField,
  },
});
</script>
