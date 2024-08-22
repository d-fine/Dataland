<template>
  <InputTextFormField
    name="nameOfProductionSite"
    description="Please state the name of the production site."
    label="Production Site"
  />
  <AddressFormField
    data-test="AddressFormField"
    name="addressOfProductionSite"
    description="Please state the address of the production site."
    label="Production Site Address"
    validation="required"
  />
  <NaceCodeFormField
    name="listOfGoodsOrServices"
    label="Lists of Goods or Services"
    description="Provide List of Goods or Services"
    data-test-list-input="listOfGoodsOrServices"
    :should-disable-checkboxes="true"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';

export default defineComponent({
  name: 'ProductionSiteFormElement',
  components: {
    NaceCodeFormField,
    InputTextFormField,
    AddressFormField,
  },
});
</script>
