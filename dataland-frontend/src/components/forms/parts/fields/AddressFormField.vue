<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <AddressFormElement
      :name="name"
      :display-name="displayName"
      :validation="validation"
      :validation-label="validationLabel ?? displayName"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import AddressFormElement from "@/components/forms/parts/elements/derived/AddressFormElement.vue";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "AddressFormField",
  components: { AddressFormElement, UploadFormHeader },
  props: FormFieldProps,
});
</script>
