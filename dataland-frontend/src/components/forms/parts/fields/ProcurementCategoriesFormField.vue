<template>
  <div class="form-field" data-test="ProcurementCategoriesFormField">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit type="group" name="procurementCategories">
      <ProcurementCategoryFormElement :name="ProcurementCategoryType.Products" label="Products" />
      <ProcurementCategoryFormElement :name="ProcurementCategoryType.RawMaterials" label="Raw Materials" />
      <ProcurementCategoryFormElement :name="ProcurementCategoryType.Services" label="Services" />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from '@formkit/vue';
import { defineComponent } from 'vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { ProcurementCategoryType } from '@/api-models/ProcurementCategoryType';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import ProcurementCategoryFormElement from '@/components/forms/parts/elements/derived/ProcurementCategoryFormElement.vue';

export default defineComponent({
  name: 'ProcurementCategoriesFormField',
  data() {
    return {
      ProcurementCategoryType,
    };
  },
  props: BaseFormFieldProps,
  components: {
    UploadFormHeader,
    ProcurementCategoryFormElement,
    FormKit,
  },
});
</script>
