<template>
  <div class="form-field">
    <UploadFormHeader v-if="label" :label="label" :description="description" :is-required="required" />
    <FormKit type="group" :name="name">
      <NumberFormField
        :name="'value'"
        :validation-label="validationLabel"
        :validation="validation"
        :unit="unit"
        input-class="col-4 pr-0"
      />
      <UploadDocumentsForm
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
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import UploadDocumentsForm from '@/components/forms/parts/elements/basic/UploadDocumentsForm.vue';
import { type DocumentToUpload } from '@/utils/FileUploadUtils';
import { isValidFileName } from '@/utils/DataSource';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';

export default defineComponent({
  name: 'BigDecimalBaseDataPointFormField',
  components: { NumberFormField, UploadFormHeader, UploadDocumentsForm },
  inheritAttrs: false,
  props: {
    ...BaseFormFieldProps,
    unit: {
      type: String,
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
    };
  },
  emits: ['fieldSpecificDocumentsUpdated'],
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
  methods: {
    /**
     * Emits event that selected document changed
     * @param updatedDocuments the updated documents that are currently selected (only one in this case)
     */
    handleDocumentUpdatedEvent(updatedDocuments: DocumentToUpload[]) {
      this.referencedDocument = updatedDocuments[0];
      this.documentName = this.referencedDocument?.fileNameWithoutSuffix;
      this.documentReference = this.referencedDocument?.fileReference;
      this.$emit('fieldSpecificDocumentsUpdated', this.referencedDocument);
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
  },
});
</script>
