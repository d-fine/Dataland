<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <RadioButtonsFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="yesNoOptions"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader },
  props: {
    ...BaseFormFieldProps,
  },

  data() {
    return {
      yesNoOptions: {
        Yes: "Yes",
        No: "No",
      },
    };
  },
});
</script>
