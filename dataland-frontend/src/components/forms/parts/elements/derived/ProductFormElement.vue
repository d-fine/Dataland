<template>
  <div data-test="productFormElement">
    <InputTextFormField
      name="name"
      label="Product Name"
      description="Please state the name of the important manufactured/distributed/offered product or service"
      :required="true"
      validation="required"
    />
    <StringBadgeListFormField
      name="productionSteps"
      label="Production Steps"
      description="Please give a brief overview of the production steps/activities"
      data-test-add-button="addProductionStep"
    />
    <FreeTextFormField
      name="relatedCorporateSupplyChain"
      label="Related Corporate Supply Chain"
      description="Please give an overview of the related corporate supply chain(s) and key business relationships (by procurement or order volume) (own operations)"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import StringBadgeListFormField from '@/components/forms/parts/fields/StringBadgeListFormField.vue';

export default defineComponent({
  name: 'ProductFormElement',
  components: {
    InputTextFormField,
    FreeTextFormField,
    StringBadgeListFormField,
  },
});
</script>
