<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <SingleSelectFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? displayName"
      :placeholder="placeholder"
      :options="options"
      inner-class="short"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "SingleSelectFormField",
  components: { SingleSelectFormElement, UploadFormHeader },
  data() {
    return {
      selection: "",
    };
  },
  props: DropdownOptionFormFieldProps,
});
</script>
