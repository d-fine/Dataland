<template>
  <FormListFormField
    :name="name"
    display-one-sub-form-per-default
    sub-form-component="PollutionEmissionFormElement"
    data-test-add-button="addPollutionEmissionButton"
    label-add-button="ADD NEW Pollution Emission"
    data-test-sub-form="PollutionEmissionSection"
    data-test-remove-button="removeItemFromListOfPollutionEmission"
  />
</template>

<script lang="ts">
import FormListFormField from "@/components/forms/parts/fields/FormListFormField.vue";
import { defineComponent } from "vue";
export default defineComponent({
  name: "PollutionEmissionFormField",
  components: {
    FormListFormField,
  },
});
</script>
