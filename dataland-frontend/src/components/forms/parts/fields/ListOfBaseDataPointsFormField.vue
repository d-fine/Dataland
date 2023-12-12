<template>
  <div class="mb-3" :data-test="name">
<!--    this inherits the data upload and the show condition on radio buttons from nested field-->
    <BaseDataPointFormField
        :name="name"
        :description="description"
        :label="label"
        :required="required"
        @reports-updated="reportsUpdated"
    >
<!--      this shows the text-->
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
      INSERT STUFF HERE
    </BaseDataPointFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";
import {BaseFormFieldProps} from "@/components/forms/parts/fields/FormFieldProps";
import type {DocumentToUpload} from "@/utils/FileUploadUtils";


export default defineComponent({
  name: "ListOfBaseDataPointsFormField",
  components: { YesNoFormField, BaseDataPointFormField },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    dataTest: String,
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
