<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    sub-form-component="ProductFormElement"
    data-test-add-button="addNewProductButton"
    label-add-button="ADD NEW Product"
    data-test-sub-form="productSection"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';

export default defineComponent({
  name: 'MostImportantProductsFormField',
  props: BaseFormFieldProps,
  components: {
    FormListFormField,
  },
});
</script>
