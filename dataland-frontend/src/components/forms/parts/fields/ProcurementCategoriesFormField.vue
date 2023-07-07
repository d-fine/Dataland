<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit type="group" name="procurementCategories">
      <ProcurementCategoryFormElement
        :existingFieldValuesCategory="WhichFieldsAreFilled[ProcurementCategoryType.Products] ?? undefined"
        :name="ProcurementCategoryType.Products"
        label="Products"
      />
      <ProcurementCategoryFormElement
        :existingFieldValuesCategory="WhichFieldsAreFilled[ProcurementCategoryType.RawMaterials] ?? undefined"
        :name="ProcurementCategoryType.RawMaterials"
        label="Raw Materials"
      />
      <ProcurementCategoryFormElement
        :existingFieldValuesCategory="WhichFieldsAreFilled[ProcurementCategoryType.Services] ?? undefined"
        :name="ProcurementCategoryType.Services"
        label="Services"
      />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { FormFieldProps } from "@/components/forms/parts/fields/BaseFormFieldProps";
import ProcurementCategoryFormElement from "@/components/forms/parts/elements/derived/ProcurementCategoryFormElement.vue";

export default defineComponent({
  name: "ProcurementCategoriesFormField",
  data() {
    return {
      ProcurementCategoryType,
      WhichFieldsAreFilled: this.existingFieldValues ?? {},
    };
  },
  props: {
    ...FormFieldProps,
    existingFieldValues: {
      type: Object,
    },
  },
  components: {
    UploadFormHeader,
    ProcurementCategoryFormElement,
    FormKit,
  },
});
</script>
