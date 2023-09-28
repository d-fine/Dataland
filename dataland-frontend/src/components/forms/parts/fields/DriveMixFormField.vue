<template>
  <div data-test="" class="form-field">
    <UploadFormHeader label="Drive mix" description="" :is-required="required" />
    <FormKit type="group" name="productsServicesCategoriesPurchased">
      <DriveMixFormElement name="smallTrucks" label="Small trucks (< 3.5t)" />
      <DriveMixFormElement name="mediumTrucks" label="Medium trucks (3.5t < x < 7.5t)" />
      <DriveMixFormElement name="largeTrucks" label="Large trucks (> 7.5t)" />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DriveMixFormElement from "@/components/forms/parts/elements/derived/DriveMixFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "DriveMixFormField",
  components: { UploadFormHeader, DriveMixFormElement },
  props: BaseFormFieldProps,
});
</script>
