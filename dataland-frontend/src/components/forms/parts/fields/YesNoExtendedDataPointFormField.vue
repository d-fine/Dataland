<template>
  <div class="mb-3" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <!-- TODO move down? -->
    <ExtendedDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :input-class="inputClass"
      :check-value-validity="hasDataPointProperValue"
    >
      <RadioButtonsFormElement
        name="value"
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
import { hasDataPointProperValue } from "@/utils/DataPoint";

export default defineComponent({
  name: "YesNoExtendedDataPointFormField",
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
    };
  },
  emits: ["reportsUpdated"],
  methods: {
    hasDataPointProperValue,
  },
});
</script>
