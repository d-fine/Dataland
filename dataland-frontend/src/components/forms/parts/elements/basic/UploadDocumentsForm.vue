<template>
  <!-- Select company reports -->
  <div class="uploaded-files" style="padding: 0; margin-left: 0">
    <FileUpload
      name="fileUpload"
      ref="fileUpload"
      accept=".pdf"
      :maxFileSize="DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES"
      invalidFileSizeMessage="{0}: Invalid file size, file size should be smaller than {1}."
      :auto="false"
      @select="handleFilesSelected"
    >
      <template #header="{ files, chooseCallback }">
        <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
          <PrimeButton
            v-if="files.length < 1 || moreThanOneDocumentAllowed"
            :data-test="'upload-files-button-' + name"
            @click="chooseCallback()"
            icon="pi pi-upload"
            label="UPLOAD DOCUMENT"
          />
        </div>
      </template>
      <template #content="{ files }">
        <div v-if="files.length > 0" data-test="files-to-upload">
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
              @click="removeDocumentFromDocumentsToUpload(index)"
              class="p-button-rounded"
            />
          </div>
        </div>
      </template>
    </FileUpload>
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import FileUpload, { FileUploadSelectEvent } from "primevue/fileupload";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import { CompanyReport } from "@clients/backend";
import Keycloak from "keycloak-js";
import {
  calculateSha256HashFromFile,
  DocumentToUpload,
  isThereActuallyANewFileSelected,
  removeFileTypeExtension,
} from "@/utils/FileUploadUtils";

export default defineComponent({
  name: "UploadDocumentsForm",
  components: {
    PrimeButton,
    FileUpload,
  },
  emits: ["documentsChanged"],
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      formsDatesFilesToUpload: [] as string[] | undefined,
      formatBytesUserFriendly,
      DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES: DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES,
      documentsToUpload: [] as DocumentToUpload[],
    };
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    referencedDocumentsForPrefill: {
      type: Object as () => { [key: string]: CompanyReport },
    },
    moreThanOneDocumentAllowed: {
      type: Boolean,
      default: true,
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
    async handleFilesSelected(event: FileUploadSelectEvent): Promise<void> {
      const selectedFilesByUser = event.files as File[];
      if (isThereActuallyANewFileSelected(selectedFilesByUser, this.documentsToUpload)) {
        const lastSelectedFile = selectedFilesByUser[selectedFilesByUser.length - 1];
        const documentToUpload = { file: lastSelectedFile } as DocumentToUpload;
        documentToUpload.reference = await calculateSha256HashFromFile(documentToUpload.file);
        documentToUpload.fileNameWithoutSuffix = removeFileTypeExtension(documentToUpload.file.name);
        this.documentsToUpload.push(documentToUpload);
        this.emitDocumentsChangedEvent();
      }
    },
    /**
     * Emits event that selected documents changed
     */
    emitDocumentsChangedEvent() {
      this.$emit("documentsChanged", this.documentsToUpload);
    },

    /**
     * Remove document from files uploaded
     * @param indexOfFileToRemove index number of the file to remove
     * @param deleteCount the number of files to delete
     */
    removeDocumentFromDocumentsToUpload(indexOfFileToRemove: number, deleteCount = 1) {
      (this.$refs.fileUpload.remove as (index: number) => void)(indexOfFileToRemove);
      this.documentsToUpload.splice(indexOfFileToRemove, deleteCount);
      this.emitDocumentsChangedEvent();
    },

    /**
     * removes all documents at once, is invoked by a yes no field if it is resetted to "No"
     */
    removeAllDocuments() {
      this.$refs.fileUpload.files = [];
      this.documentsToUpload = [];
      this.emitDocumentsChangedEvent();
    },

    /**
     * Prefills the File upload with dummy files to get the file upload to display filenames of files that are already
     * referenced in a dataset (in the case of editing a dataset). These dummy files do not get picked up in the upload
     * process because they do not trigger a FileUploadSelectEvent.
     * @param fileNames the names that should be display by the fileUpload
     */
    prefillFileUpload(fileNames: string[]) {
      fileNames.forEach((name) => {
        const dummyFile = new File([] as BlobPart[], name);
        this.$refs.fileUpload.files.push(dummyFile);
      });
    },
  },
});
</script>

<style scoped>
.p-button-edit-reports {
  width: 1rem;
  border-radius: 50%;
  height: 1rem;
  padding: 12px;
}
</style>
