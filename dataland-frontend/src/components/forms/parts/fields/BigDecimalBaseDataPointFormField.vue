<template>
  <UploadFormHeader v-if="label" :label="label" :description="description" :is-required="required" />
  <FormKit type="group" :name="name">
    <FormKit
      type="text"
      name="value"
      :unit="unit"
      :value="currentValue"
      :validation-label="validationLabel ?? label"
      :validation="`number|${validation}`"
      :placeholder="unit ? `Value in ${unit}` : 'Value'"
      :validationMessages="{ integer: `${validationLabel ?? label} must be an integer.` }"
      :outer-class="inputClass"
      @input="updateShowButton($event)"
    />
    <UploadDocumentsForm
      v-if="showUploadButton"
      @updatedDocumentsSelectedForUpload="handleDocumentUpdatedEvent"
      ref="uploadDocumentsForm"
      name="name"
      :more-than-one-document-allowed="false"
      :file-names-for-prefill="fileNamesForPrefill"
    />

    <FormKit v-if="isValidFileName(isMounted, documentName)" type="group" name="dataSource">
      <FormKit type="hidden" name="fileName" v-model="documentName" />
      <FormKit type="hidden" name="fileReference" v-model="documentReference" />
    </FormKit>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import { isValidFileName } from "@/utils/DataSource";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "BigDecimalBaseDataPointFormField",
  components: { UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    unit: {
      type: String,
    },
    currentValue: String,
  },
  inject: {
    injectlistOfFilledKpis: {
      from: "listOfFilledKpis",
      default: [] as Array<string>,
    },
  },
  data() {
    return {
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: undefined as string | undefined,
      documentReference: undefined as string | undefined,
      fileNamesForPrefill: [] as string[],
      isMounted: false,
      isValidFileName: isValidFileName,
      showButton: (this.injectlistOfFilledKpis as unknown as Array<string>).includes(this.name as string),
    };
  },
  emits: ["fieldSpecificDocumentsUpdated"],
  mounted() {
    setTimeout(() => {
      this.updateFileUploadFiles();
      this.isMounted = true;
    });
  },
  watch: {
    documentName() {
      if (this.isMounted) {
        this.updateFileUploadFiles();
      }
    },
  },
  computed: {
    showUploadButton(): boolean {
      return this.showButton;
    },
  },
  methods: {
    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix;
      this.documentReference = this.referencedDocument?.fileReference;
      this.$emit("fieldSpecificDocumentsUpdated", this.referencedDocument);
    },

    /**
     * updates the files in the fileUpload file list to represent that a file was already uploaded in a previous upload
     * of the given dataset (in the case of editing a dataset)
     */
    updateFileUploadFiles() {
      if (this.documentName !== undefined && this.referencedDocument === undefined) {
        this.fileNamesForPrefill = [this.documentName];
      }
    },
    /**
     * Updates the boolean to controll if the upload documents component should be shown or not
     * @param input input event of the corresponding input form field
     */
    updateShowButton(input: string) {
      this.showButton = !!input;
    },
  },
});
</script>
