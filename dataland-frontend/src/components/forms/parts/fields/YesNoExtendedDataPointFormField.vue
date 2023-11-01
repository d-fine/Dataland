<template>
  <div class="mb-3" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <ExtendedDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :inner-class="innerClass"
      :is-data-value-provided="currentValue != '' && currentValue != undefined"
    >
      <RadioButtonsFormElement
        name="value"
        v-model:currentValue="currentValue"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :options="yesNoOptions"
        :data-test="dataTest"
      />
    </ExtendedDataPointFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import ExtendedDataPointFormField from "@/components/forms/parts/elements/basic/ExtendedDataPointFormField.vue";

export default defineComponent({
  name: "YesNoFormField",
  components: { ExtendedDataPointFormField, RadioButtonsFormElement, UploadFormHeader },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
  },

  data() {
    return {
      yesNoOptions: {
        Yes: "Yes",
        No: "No",
      },
      currentValue: undefined,
    };
  },
  emits: ["reportsUpdated"],
});
</script>
