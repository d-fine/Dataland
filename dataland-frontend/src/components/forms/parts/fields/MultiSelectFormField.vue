<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <MultiSelectFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      inner-class="short"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";

import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MultiSelectFormField",
  components: { MultiSelectFormElement, UploadFormHeader },
  data() {
    return {
      selections: [] as string[],
    };
  },
  props: DropdownOptionFormFieldProps,
});
</script>
