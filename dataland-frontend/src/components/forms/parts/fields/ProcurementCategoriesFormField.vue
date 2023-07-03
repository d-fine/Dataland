<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit type="group" name="procurementCategories">
      <ProductCategoriesFormElement :name="ProcurementCategoryType.Products" label="Products" />
      <ProductCategoriesFormElement :name="ProcurementCategoryType.RawMaterials" label="Raw Materials" />
      <ProductCategoriesFormElement :name="ProcurementCategoryType.Services" label="Services" />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ProductCategoriesFormElement from "@/components/forms/parts/elements/derived/ProductCategoriesFormElement.vue";

export default defineComponent({
  name: "ProcurementCategoriesFormField",
  data() {
    return {
      ProcurementCategoryType,
    };
  },
  props: FormFieldProps,
  components: {
    UploadFormHeader,
    ProductCategoriesFormElement,
  },
});
</script>
