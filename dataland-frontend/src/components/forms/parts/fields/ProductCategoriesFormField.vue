<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <ProductCategoriesFormElement :name="ProcurementCategory.Products" label="Products" />
    <ProductCategoriesFormElement :name="ProcurementCategory.RawMaterials" label="Raw Materials" />
    <ProductCategoriesFormElement :name="ProcurementCategory.Services" label="Services" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { ProcurementCategory } from "@/api-models/ProcurementCategory";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import ProductCategoriesFormElement from "@/components/forms/parts/elements/derived/ProductCategoriesFormElement.vue";

export default defineComponent({
  name: "ProductCategoriesFormField",
  data() {
    return {
      ProcurementCategory,
    };
  },
  props: FormFieldProps,
  components: {
    UploadFormHeader,
    ProductCategoriesFormElement,
  },
});
</script>
