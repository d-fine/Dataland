<template>
  <div :class="classes" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <RadioButtonsFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="HumanizedYesNoNa"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { HumanizedYesNoNa } from "@/utils/YesNoNa";

export default defineComponent({
  name: "YesNoNaFormField",
  computed: {
    HumanizedYesNoNa() {
      return HumanizedYesNoNa;
    },
  },
  components: { RadioButtonsFormElement, UploadFormHeader },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    classes: {
      type: String,
      default: "form-field",
    },
  },
});
</script>
