<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    sub-form-component="NonAlignedActivitiesFormElement"
    data-test-add-button="addNewProductButton"
    label-add-button="ADD NEW Activity"
    data-test-sub-form="alignedActivitiesSection"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';

export default defineComponent({
  name: 'NonAlignedActivitiesFormField',
  props: BaseFormFieldProps,
  components: {
    FormListFormField,
  },
});
</script>
