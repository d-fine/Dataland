<template>
  <div class="mb-3" :data-test="name">
    <BaseDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      @field-specific-documents-updated="fieldSpecificDocumentsUpdated"
    >
      <YesNoFormField
        name="value"
        :label="label"
        :description="description"
        :is-required="required"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :radio-buttons-data-test="dataTest"
        classes=""
      />
    </BaseDataPointFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";

export default defineComponent({
  name: "YesNoBaseDataPointFormField",
  components: { YesNoFormField, BaseDataPointFormField },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
  },
  emits: ["fieldSpecificDocumentsUpdated"],
  methods: {
    /**
     * Emits event that the selected document changed
     * @param referencedDocument the new referenced document
     */
    fieldSpecificDocumentsUpdated(referencedDocument: DocumentToUpload | undefined) {
      this.$emit("fieldSpecificDocumentsUpdated", referencedDocument);
    },
  },
});
</script>
