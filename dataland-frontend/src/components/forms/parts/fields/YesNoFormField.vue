<template>
  <RadioButtonsFormField
    :name="name"
    :info="info"
    :validation="validation"
    :validation-label="validationLabel ?? displayName"
    :display-name="displayName"
    :options="[
      {
        label: 'Yes',
        value: 'Yes',
      },
      {
        label: 'No',
        value: 'No',
      },
    ]"
    :required="required"
  ></RadioButtonsFormField>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormField },
  inheritAttrs: false,
  props: {
    name: {
      type: String,
      default: "",
    },
    info: {
      type: String,
      default: "",
    },
    displayName: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
    required: {
      type: Boolean,
      default: false,
    },
    validationLabel: {
      type: String,
    },
  },
});
</script>
