<template>
  <div class="form-field">
    <InputTextFormField
      name="nameOfProductionSite"
      description="Please state the name of the production site."
      label="Production Site"
    />
  </div>

  <div class="form-field">
    <AddressFormField
      :data-test="`AddressFormField${id}`"
      name="addressOfProductionSite"
      description="Please state the address of the production site."
      label="Production Site Address"
      validation="required"
    />
  </div>

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
  props: {
    id: {
      type: String,
      required: true,
    },
  },
});
</script>
