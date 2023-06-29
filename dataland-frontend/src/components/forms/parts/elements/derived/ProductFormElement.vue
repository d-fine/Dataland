<template>
  <div data-test="productFormElement">
    <div class="form-field">
      <InputTextFormField
        name="productName"
        label="Product Name"
        description="Please enter the name of the product"
        :required="true"
        validation="required"
      />
    </div>

    <StringBadgeListFormField
      name="productionSteps"
      label="Production Steps"
      description="Please give a brief overview of the production steps/activities undertaken"
      data-test-add-button="addProductionStep"
    />

    <div class="form-field">
      <UploadFormHeader
        label="Related Corporate Supply Chain"
        description="Please give an overview of the related corporate supply chain(s) and key business relationships (by procurement or order volume) (own operations)"
      />
      <FreeTextFormField name="relatedCorporateSupplyChain" v-model="existingRelatedCorporateSupplyChain" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import StringBadgeListFormField from "@/components/forms/parts/fields/StringBadgeListFormField.vue";

export default defineComponent({
  name: "ProductFormElement",
  components: {
    UploadFormHeader,
    InputTextFormField,
    FreeTextFormField,
    StringBadgeListFormField,
  },
  data() {
    return {
      existingRelatedCorporateSupplyChain: "",
    };
  },
});
</script>
