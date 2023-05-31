<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <FormKit v-if="certificateRequiredIfYes" type="group" :name="name">
      <RadioButtonsFormElement
        name="value"
        :validation="validation"
        :validation-label="validationLabel ?? label"
        :options="[
          {
            label: 'Yes',
            value: 'Yes',
          },
          {
            label: 'No',
            value: 'No',
          },
        ]"
        :data-test="dataTest"
        @input="setDocumentRequired($event)"
      />
      <UploadDocumentsForm
        v-show="yesSelected"
        @documentsChanged="handleDocumentUpdatedEvent"
        ref="uploadDocumentsForm"
        :name="name"
        :more-than-one-document-allowed="false"
        :file-names-for-prefill="fileNamesForPrefill"
      />
      <FormKit v-if="yesSelected" type="group" name="dataSource">
        <FormKit type="hidden" name="name" v-model="documentName" />
        <FormKit
          type="text"
          name="reference"
          v-model="documentReference"
          validation="required"
          validation-label="If 'Yes' is selected an uploaded document"
          :outer-class="{ 'hidden-input': true }"
        />
      </FormKit>
    </FormKit>

    <RadioButtonsFormElement
      v-else
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="[
        {
          label: 'Yes',
          value: 'Yes',
        },
        {
          label: 'No',
          value: 'No',
        },
      ]"
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

export default defineComponent({
  name: "YesNoFormField",
  components: { RadioButtonsFormElement, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: { ...YesNoFormFieldProps, dataTest: String },
  data() {
    return {
      yesSelected: false,
      referencedDocument: {} as DocumentToUpload,
      documentName: "",
      documentReference: "",
      fileNamesForPrefill: [] as string[],
    };
  },

  emits: ["documentUpdated"],
  mounted() {
    this.updateFileUploadFiles();
  },
  watch: {
    yesSelected() {
      this.deleteDocument();
    },
    documentName() {
      this.updateFileUploadFiles();
    },
  },
  methods: {
    /**
     * Sets the value yesSelected to true when "Yes" is selected
     * @param event the "Yes" / "No" selection event
     */
    setDocumentRequired(event: Event) {
      this.yesSelected = (event as unknown as string) === "Yes";
    },

    /**
     * If "No" is reselected after one has uploaded a file, this handles removing the file(s) and clearing
     * the certificate list.
     */
    deleteDocument() {
      if (!this.yesSelected) {
        (this.$refs.uploadDocumentsForm.removeAllDocuments as () => void)();
      }
    },

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
      if (this.documentName !== "" && Object.keys(this.referencedDocument).length == 0) {
        this.fileNamesForPrefill.push(this.documentName);
      }
    },
  },
});
</script>
