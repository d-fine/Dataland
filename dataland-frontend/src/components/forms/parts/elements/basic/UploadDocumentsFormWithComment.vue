<template>
  <div class="form-field" data-test="UploadDocumentsFormWithComment">
    <FormKit v-model="baseDataPoint" type="group" name="dataSource">
      <UploadDocumentsForm
        @updatedDocumentsSelectedForUpload="handleDocumentUpdatedEvent"
        ref="uploadDocumentsForm"
        name="name"
        :more-than-one-document-allowed="false"
        :file-names-for-prefill="fileNamesForPrefill"
      />
      <FormKit type="hidden" name="fileName" v-model="documentName" />
      <FormKit type="hidden" name="fileReference" v-model="documentReference" />
    </FormKit>
    <FormKit
      type="textarea"
      name="value"
      placeholder="(Optional) Add comment that might help Quality Assurance to approve the datapoint. "
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { type DocumentToUpload } from "@/utils/FileUploadUtils";
import { type BaseDataPointString } from "@clients/backend";

export default defineComponent({
  name: "UploadDocumentsFormWithComment",
  components: { UploadDocumentsForm },
  inheritAttrs: false,
  data() {
    return {
      baseDataPoint: {} as BaseDataPointString,
      referencedDocument: undefined as DocumentToUpload | undefined,
      documentName: "",
      documentReference: "",
      fileNamesForPrefill: [] as string[],
      isMounted: false,
    };
  },
  emits: ["fieldSpecificDocumentsUpdated"],
  mounted() {
    this.updateFileUploadFiles();
    this.isMounted = true;
  },
  watch: {
    documentName() {
      if (this.isMounted) {
        this.updateFileUploadFiles();
      }
    },
  },
  methods: {
    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix ?? "";
      this.documentReference = this.referencedDocument?.fileReference ?? "";
      this.$emit("fieldSpecificDocumentsUpdated", this.referencedDocument);
    },

    /**
     * updates the files in the fileUpload file list to represent that a file was already uploaded in a previous upload
     * of the given dataset (in the case of editing a dataset)
     */
    updateFileUploadFiles() {
      if (this.documentName !== "" && this.referencedDocument === undefined) {
        this.fileNamesForPrefill = [this.documentName];
      }
    },
  },
});
</script>
