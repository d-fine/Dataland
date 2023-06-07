<template>
  <div class="form-field">
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
        {
          label: 'N/A',
          value: 'NA',
        },
      ]"
      :required="required"
    ></RadioButtonsFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "YesNoNaFormField",
  components: { RadioButtonsFormField },
  inheritAttrs: false,
  props: YesNoFormFieldProps,
});
</script>
