<template>
  <div class="mb-3" :data-test="name">
    <BaseDataPointFormField
      :name="name"
      :description="description"
      :label="label"
      :required="required"
      :options="HumanizedYesNoNa"
      @reports-updated="reportsUpdated"
    >
      <YesNoNaFormField
        name="value"
        :label="label"
        :description="description"
        :is-required="required"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        classes=""
      />
    </BaseDataPointFormField>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import BaseDataPointFormField from "@/components/forms/parts/elements/basic/BaseDataPointFormField.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import YesNoNaFormField from "@/components/forms/parts/fields/YesNoNaFormField.vue";
import { HumanizedYesNoNa } from "@/utils/YesNoNa";

export default defineComponent({
  name: "YesNoNaBaseDataPointFormField",
  components: { YesNoNaFormField, BaseDataPointFormField },
  inheritAttrs: false,
  props: { ...BaseFormFieldProps },
  emits: ["reportsUpdated"],
  data() {
    return {
      HumanizedYesNoNa,
    }
  },
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
