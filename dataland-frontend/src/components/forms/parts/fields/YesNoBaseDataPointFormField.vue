<template>
  <div class="mb-3" :data-test="`BaseDataPointFormField${name}`">
    <BaseDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :options="HumanizedYesNo"
      @reports-updated="reportsUpdated"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";

import { HumanizedYesNo } from "@/utils/YesNoNa";

export default defineComponent({
  name: "YesNoBaseDataPointFormField",
  components: { BaseDataPointFormField },
  inheritAttrs: false,
  data() {
    return {
      HumanizedYesNo,
    };
  },
  props: {
    ...BaseFormFieldProps,
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
