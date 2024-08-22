<template>
  <InputTextFormField
    name="nameOfSubsidiary"
    description="Please state the name of the subsidiary."
    label="Subsidiary"
    validation="required"
  />
  <AddressFormField
    data-test="AddressFormField"
    name="addressOfSubsidiary"
    description="Please state the address of the subsidiary."
    label="Subsidiary Address"
    validation="required"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';

export default defineComponent({
  name: 'SubsidiaryFormElement',
  components: {
    InputTextFormField,
    AddressFormField,
  },
});
</script>
