<template>
  <FormListFormField
    :name="name"
    display-one-sub-form-per-default
    sub-form-component="SiteAndAreaFormElement"
    data-test-add-button="addPSiteAndAreaButton"
    label-add-button="ADD NEW Site And Area"
    data-test-sub-form="SiteAndAreaSection"
    data-test-remove-button="removeItemFromListOfSiteAndArea"
  />
</template>

<script lang="ts">
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
export default defineComponent({
  name: 'SiteAndAreaFormField',
  components: {
    FormListFormField,
  },
  props: BaseFormFieldProps,
});
</script>
