<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit v-if="certificateRequiredIfYes" v-model="baseDataPointYesNo" type="group" :name="name">
      <RadioButtonsFormElement
        name="value"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :options="yesNoOptions"
        :data-test="dataTest"
      />
      <UploadDocumentsForm
        v-show="baseDataPointYesNo.value === 'Yes'"
        @documentsChanged="handleDocumentUpdatedEvent"
        ref="uploadDocumentsForm"
        :name="name"
        :more-than-one-document-allowed="false"
        :file-names-for-prefill="fileNamesForPrefill"
      />
      <FormKit v-if="baseDataPointYesNo.value === 'Yes'" type="group" name="dataSource">
        <FormKit type="hidden" name="name" v-model="documentName" />
        <FormKit type="text" name="reference" v-model="documentReference" :outer-class="{ 'hidden-input': true }" />
      </FormKit>
    </FormKit>

    <RadioButtonsFormElement
      v-else
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="yesNoOptions"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { YesNoFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { DocumentToUpload } from "@/utils/FileUploadUtils";
import { BaseDataPointYesNo } from "@clients/backend";

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: { ...YesNoFormFieldProps, dataTest: String },
  data() {
    return {
      baseDataPointYesNo: {} as BaseDataPointYesNo,
      referencedDocument: {} as DocumentToUpload,
      documentName: "",
      documentReference: "",
      fileNamesForPrefill: [] as string[],
      yesNoOptions: {
        "Yes": "Yes",
        "No": "No",
      }
    };
  },

  emits: ["documentUpdated"],
  mounted() {
    console.log("mounted"); // TODO
    this.updateFileUploadFiles();
  },
  watch: {
    baseDataPointYesNo(newValue: BaseDataPointYesNo) {
      if (newValue.value && newValue.value === "No") {
        (this.$refs.uploadDocumentsForm.removeAllDocuments as () => void)();
      }
    },
    documentName() {
      this.updateFileUploadFiles();
    },
  },
  methods: {
    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = updatedDocuments[0]?.fileNameWithoutSuffix ?? "";
      this.documentReference = updatedDocuments[0]?.reference ?? "";
      this.$emit("documentUpdated", this.name, updatedDocuments[0]);
    },

    /**
     * updates the files in the fileUpload file list to represent that a file was already uploaded in a previous upload
     * of the given dataset (in the case of editing a dataset)
     */
    updateFileUploadFiles() {
      console.log("updateFileUploadFiles"); // TODO
      if (this.documentName !== "" && Object.keys(this.referencedDocument).length == 0) {
        console.log("if is valid"); // TODO
        this.fileNamesForPrefill.push(this.documentName);
      }
    },
  },
});
</script>
