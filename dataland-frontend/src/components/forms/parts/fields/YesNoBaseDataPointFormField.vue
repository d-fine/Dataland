<template>
  <div class="mb-3" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
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
        :options="yesNoOptions"
        :data-test="dataTest"
      />
    </BaseDataPointFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "YesNoBaseDataPointFormField",
  components: { UploadFormHeader, BaseDataPointFormField, RadioButtonsFormElement },
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
