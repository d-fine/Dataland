<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    sub-form-component="ProductionSiteFormElement"
    data-test-add-button="addNewProductionSiteButton"
    label-add-button="ADD NEW Production Site"
    data-test-sub-form="productionSiteSection"
    data-test-remove-button="removeItemFromListOfProductionSites"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import FormListFormField from '@/components/forms/parts/fields/FormListFormField.vue';

export default defineComponent({
  name: 'ProductionSitesFormField',
  props: BaseFormFieldProps,
  components: {
    FormListFormField,
  },
});
</script>
