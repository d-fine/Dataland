<template>
    <div class="form-field">
        <UploadFormHeader :label="label" :description="description" :is-required="required" />
        <ProductCategory :name="ProcurementCategory.Products" label="Products" />
        <ProductCategory :name="ProcurementCategory.RawMaterials" label="Raw Materials" />
        <ProductCategory :name="ProcurementCategory.Services" label="Services" />
    </div>
</template>

<script>
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import {ProcurementCategory} from "@/api-models/ProcurementCategory";

export default defineComponent({
    name: "ProductCategoriesFormField",
    data() {
        return {
            ProcurementCategory,
        }
    },
    components: {
      UploadFormHeader,
    },
});
</script>
