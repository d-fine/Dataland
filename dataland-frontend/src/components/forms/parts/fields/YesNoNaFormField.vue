<template>
  <div class="form-field">
    <RadioButtonsFormField
      :name="name"
      :description="description"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :label="label"
      :options="{
        Yes: 'Yes',
        No: 'No',
        NA: 'N/A',
      }"
      :required="required"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/BaseFormFieldProps";

export default defineComponent({
  name: "YesNoNaFormField",
  components: { RadioButtonsFormField },
  inheritAttrs: false,
  props: YesNoFormFieldProps,
});
</script>
