<template>
  <BaseDataPointFormField
    :name="name"
    :description="description"
    :label="label"
    :required="required"
    @reports-updated="reportsUpdated"
  >
    <RadioButtonsFormElement
      name="value"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="yesNoNaOptions"
    />
  </BaseDataPointFormField>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";

export default defineComponent({
  name: "YesNoNaBaseDataPointFormField",
  components: { BaseDataPointFormField, RadioButtonsFormElement },
  inheritAttrs: false,
  props: { ...BaseFormFieldProps },
  data() {
    return {
      yesNoNaOptions: {
        Yes: "Yes",
        No: "No",
        NA: "N/A",
      },
    };
  },
  emits: ["reportsUpdated"],
  methods: {
    /**
     * Emits event that the selected document changed
     * @param documentName the name of the new referenced document
     * @param referencedDocument the new referenced document
     */
    reportsUpdated(documentName: string, referencedDocument: DocumentToUpload | undefined) {
      this.$emit("reportsUpdated", documentName, referencedDocument);
    },
  },
});
</script>
