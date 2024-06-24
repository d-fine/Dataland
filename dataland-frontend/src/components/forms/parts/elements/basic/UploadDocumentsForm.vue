<template>
  <!-- Select company reports -->
  <div
    id="uploadReports"
    class="uploaded-files"
    style="padding: 0; margin-left: 0"
    :data-test="`upload-documents-${name}`"
  >
    <FileUpload
      name="fileUpload"
      ref="fileUpload"
      :maxFileSize="DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES"
      :invalidFileSizeMessage="`{0}: Invalid file size, file size should be smaller than ${
        DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES / BYTE_TO_MEGABYTE_FACTOR
      } MB.`"
      :auto="false"
      :multiple="moreThanOneDocumentAllowed"
      @select="handleFilesSelected"
    >
      <template #header="{ files, chooseCallback }">
        <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
          <PrimeButton
            v-show="files.length < 1 || moreThanOneDocumentAllowed"
            :data-test="'upload-files-button-' + name"
            @click="chooseCallback()"
            icon="pi pi-upload"
            label="UPLOAD DOCUMENT"
          />
        </div>
      </template>
      <template #content="{ files, messages }">
        <FileSelectMessage v-for="msg of messages" :key="msg" severity="error">{{ msg }} </FileSelectMessage>
        <div v-show="files.length > 0" data-test="files-to-upload">
          <div
            v-for="(selectedFile, index) of files"
            :key="selectedFile.name + index"
            class="flex w-full align-items-center file-upload-item"
            :data-test="removeFileTypeExtension(selectedFile.name) + 'FileUploadContainer'"
          >
            <span data-test="files-to-upload-title" class="font-semibold flex-1">{{ selectedFile.name }}</span>
            <div v-if="selectedFile.size > 0" data-test="files-to-upload-size" class="mx-2 text-black-alpha-50">
              {{ formatBytesUserFriendly(selectedFile.size, 1) }}
            </div>
            <div v-else data-test="currently-uploaded-text" class="mx-2 text-black-alpha-50">Currently uploaded</div>

            <PrimeButton
              data-test="files-to-upload-remove"
              icon="pi pi-times"
              @click="removeDocumentsFromDocumentsToUpload([index])"
              class="p-button-rounded"
            />
          </div>
        </div>
      </template>
    </FileUpload>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import FileUpload, { type FileUploadSelectEvent } from 'primevue/fileupload';
import { formatBytesUserFriendly } from '@/utils/NumberConversionUtils';
import {
  calculateSha256HashFromFile,
  type DocumentToUpload,
  isThereActuallyANewFileSelected,
  removeFileTypeExtension,
} from '@/utils/FileUploadUtils';
import { DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES, BYTE_TO_MEGABYTE_FACTOR } from '@/DatalandSettings';
import FileSelectMessage from 'primevue/message';

export default defineComponent({
  name: 'UploadDocumentsForm',
  components: {
    FileSelectMessage,
    PrimeButton,
    FileUpload,
  },
  emits: ['updatedDocumentsSelectedForUpload'],
  data() {
    return {
      formatBytesUserFriendly,
      DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES,
      BYTE_TO_MEGABYTE_FACTOR,
      documentsToUpload: [] as DocumentToUpload[],
    };
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    moreThanOneDocumentAllowed: {
      type: Boolean,
      default: true,
    },
    fileNamesForPrefill: {
      type: Array,
    },
  },
  watch: {
    fileNamesForPrefill() {
      this.prefillFileUpload();
    },
  },
  methods: {
    removeFileTypeExtension,
    /**
     * Handles selection of a file by the user. Only considers the file that was added last.
     * The file is added to the documents that shall be uploaded, then the sha256 hashes are calculated and added
     * to the respective files.
     * @param event full event object containing the files
     * @param event.files files
     */
    handleFilesSelected(event: FileUploadSelectEvent) {
      const selectedFilesByUser = event.files as File[];
      if (isThereActuallyANewFileSelected(selectedFilesByUser, this.documentsToUpload)) {
        const documentsToUpload = Promise.all(
          selectedFilesByUser.map(async (file) => {
            return {
              file: file,
              fileReference: await calculateSha256HashFromFile(file),
              fileNameWithoutSuffix: removeFileTypeExtension(file.name),
            };
          })
        ) as Promise<DocumentToUpload[]>;

        void documentsToUpload.then((documentsToUpload) => {
          this.documentsToUpload = documentsToUpload;
          this.emitUpdatedDocumentsSelectionEvent();
        });
      }
    },
    /**
     * Emits event that selected documents changed
     */
    emitUpdatedDocumentsSelectionEvent() {
      this.$emit('updatedDocumentsSelectedForUpload', this.documentsToUpload);
    },

    /**
     * Remove documents from files uploaded
     * @param indexesOfFilesToRemove index list of numbers of the files to remove
     */
    removeDocumentsFromDocumentsToUpload(indexesOfFilesToRemove: number[]) {
      indexesOfFilesToRemove.sort((a, b) => b - a);
      const sortedIndexes = [...indexesOfFilesToRemove];
      [...new Set(sortedIndexes)].forEach((indexOfFileToRemove) => {
        ((this.$refs.fileUpload as FileUpload)?.remove as (index: number) => void)(indexOfFileToRemove);
        this.documentsToUpload.splice(indexOfFileToRemove, 1);
      });
      this.emitUpdatedDocumentsSelectionEvent();
    },

    /**
     * removes all documents at once, is invoked by a yes no field if it is resetted to "No"
     */
    // Called from YesNoFormField
    // eslint-disable-next-line vue/no-unused-properties
    removeAllDocuments() {
      (this.$refs.fileUpload as FileUpload).files = [];
      this.documentsToUpload = [];
      this.emitUpdatedDocumentsSelectionEvent();
    },

    /**
     * Prefills the File upload with dummy files to get the file upload to display filenames of files that are already
     * referenced in a dataset (in the case of editing a dataset). These dummy files do not get picked up in the upload
     * process because they do not trigger a FileUploadSelectEvent.
     */
    prefillFileUpload() {
      if (this.fileNamesForPrefill) {
        this.fileNamesForPrefill.forEach((name) => {
          const dummyFile = new File([] as BlobPart[], name as string);

          ((this.$refs.fileUpload as FileUpload)?.files as File[])?.push(dummyFile);
        });
      }
    },
  },
});
</script>
