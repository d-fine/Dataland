<template>
  <FormListFormField
    :name="name"
    :label="label"
    :description="description"
    :required="required"
    :validation="validation"
    :validation-label="validationLabel"
    display-one-by-default
    sub-form-component="ProductionSiteFormElement"
    data-test-add-button="addNewProductionSiteButton"
    label-add-button="ADD NEW Production Site"
    data-test-sub-form="productionSiteSection"
    data-test-remove-button="removeItemFromListOfProductionSites"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormFieldProps } from "@/components/forms/parts/fields/BaseFormFieldProps";
import FormListFormField from "@/components/forms/parts/fields/FormListFormField.vue";

export default defineComponent({
  name: "ProductionSitesFormField",
  props: FormFieldProps,
  components: {
    FormListFormField,
  },
});
</script>
