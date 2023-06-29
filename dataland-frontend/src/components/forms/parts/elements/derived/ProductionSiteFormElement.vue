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
  <StringBadgeList
    name="listOfGoodsOrServices"
    label="Lists of Goods or Services"
    description="Provide List of Goods or Services"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import StringBadgeList from "@/components/forms/parts/fields/StringBadgeListFormField.vue";

export default defineComponent({
  name: "ProductionSiteFormElement",
  components: {
    StringBadgeList,
    InputTextFormField,
    AddressFormField,
  },
});
</script>
