<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <RadioButtonsFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="yesNoNaOptions"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "YesNoNaFormField",
  components: { RadioButtonsFormElement, UploadFormHeader },
  inheritAttrs: false,
  props: { ...YesNoFormFieldProps },
  data() {
    return {
      yesNoNaOptions: {
        Yes: "Yes",
        No: "No",
        NA: "N/A",
      },
      isMounted: false,
    };
  },
});
</script>
