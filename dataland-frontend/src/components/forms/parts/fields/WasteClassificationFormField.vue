<template>
  <FormListFormField
    :name="name"
    display-one-sub-form-per-default
    sub-form-component="WasteClassificationFormElement"
    data-test-add-button="addWasteClassificationButton"
    label-add-button="ADD NEW Waste Classification"
    data-test-sub-form="WasteClassificationSection"
    data-test-remove-button="removeItemFromListOfWasteClassification"
  />
</template>

<script lang="ts">
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
export default defineComponent({
  name: 'WasteClassificationFormField',
  components: {
    FormListFormField,
  },
  props: BaseFormFieldProps,
});
</script>
