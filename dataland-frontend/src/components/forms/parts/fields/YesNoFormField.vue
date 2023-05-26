<template>
  <RadioButtonsFormField
    :name="name"
    :description="description"
    :validation="validation"
    :validation-label="validationLabel ?? label"
    :label="label"
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
    :data-test="dataTest"
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
  props: { ...FormFieldProps, dataTest: String },
});
</script>
