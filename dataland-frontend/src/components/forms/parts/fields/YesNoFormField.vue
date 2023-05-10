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
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormField },
  inheritAttrs: false,
  props: FormFieldProps,
});
</script>
