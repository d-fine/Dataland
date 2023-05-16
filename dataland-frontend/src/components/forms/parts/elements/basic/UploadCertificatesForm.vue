<template>
  <!-- Select company reports -->
  <div class="col-9 uploaded-files" style="padding: 0; margin-left: 0">
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
        <PrimeButton
          v-show="files.length < 1"
          data-test="upload-files-button"
          @click="chooseCallback()"
          icon="pi pi-upload"
          label="UPLOAD DOCUMENT"
        />
      </template>
      <template #content="{ files, removeFileCallback }">
        <div v-if="files.length > 0" data-test="files-to-upload">
          <div
            v-for="(selectedFile, index) of files"
            :key="selectedFile.name + index"
            class="flex w-full align-items-center file-upload-item"
            style="padding: 0; margin-top: 0; margin-bottom: 0"
            :data-test="removeFileTypeExtension(selectedFile.name) + 'FileUploadContainer'"
          >
            <span data-test="files-to-upload-title" class="font-semibold flex-1">{{ selectedFile.name }}</span>
            <div data-test="files-to-upload-size" class="mx-2 text-black-alpha-50">
              {{ formatBytesUserFriendly(selectedFile.size, 1) }}
            </div>
            <PrimeButton
              data-test="files-to-upload-remove"
              icon="pi pi-times"
              @click="removeCertificateFromCertificatesToUpload(removeFileCallback, index)"
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
  CertificateToUpload,
  isThereActuallyANewFileSelected,
  removeFileTypeExtension,
  StoredReport,
} from "@/utils/FileUploadUtils";

export default defineComponent({
  name: "UploadCertificatesForm",
  components: {
    PrimeButton,
    FileUpload,
  },
  emits: ["certificatesChanged"],
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
      certificatesToUpload: [] as CertificateToUpload[],
      storedReports: [] as StoredReport[],
    };
  },
  props: {
    referencedReportsForPrefill: {
      type: Object as () => { [key: string]: CompanyReport },
    },
  },
  computed: {
    allReferenceableReportNames(): string[] {
      const namesOfFilesToUpload = this.certificatesToUpload.map(
        (certificateToUpload) => certificateToUpload.fileNameWithoutSuffix
      );
      const namesOfStoredReports = this.storedReports.map((storedReport) => storedReport.reportName);
      return namesOfFilesToUpload.concat(namesOfStoredReports);
    },
  },
  watch: {
    referencedReportsForPrefill() {
      this.prefillAlreadyUploadedReports();
    },
  },
  methods: {
    removeFileTypeExtension,
    /**
     * Handles selection of a file by the user.
     * The file is added to the reports that shall be uploaded, then the sha256 hashes are calculated and added
     * to the respective files.
     * @param event full event object containing the files
     * @param event.files files
     */
    async handleFilesSelected(event: FileUploadSelectEvent): Promise<void> {
      const selectedFilesByUser = event.files as File[];
      if (isThereActuallyANewFileSelected(selectedFilesByUser, this.certificatesToUpload)) {
        const lastSelectedFile = selectedFilesByUser[selectedFilesByUser.length - 1];
        const certificateToUpload = { file: lastSelectedFile } as CertificateToUpload;
        certificateToUpload.reference = await calculateSha256HashFromFile(certificateToUpload.file);
        certificateToUpload.fileNameWithoutSuffix = removeFileTypeExtension(certificateToUpload.file.name);
        this.certificatesToUpload.push(certificateToUpload);
        this.emitCertificatesChangedEvent();
      }
    },
    /**
     * Emits event that selected certificates changed
     */
    emitCertificatesChangedEvent() {
      this.$emit("certificatesChanged", this.certificatesToUpload);
    },

    /**
     * Remove certificate from files uploaded
     * @param fileRemoveCallback Callback function removes certificate from the ones selected in formKit
     * @param indexOfFileToRemove index number of the file to remove
     * @param deleteCount the number of files to delete
     */
    removeCertificateFromCertificatesToUpload(
      fileRemoveCallback: (x: number) => void,
      indexOfFileToRemove: number,
      deleteCount = 1
    ) {
      fileRemoveCallback(indexOfFileToRemove);
      this.certificatesToUpload.splice(indexOfFileToRemove, deleteCount);
      this.emitCertificatesChangedEvent();
    },

    /**
     * removes all certificates at once, is invoked by a yes no field if it is resetted to "No"
     */
    removeAllCertificates() {
      this.certificatesToUpload = [];
      this.emitCertificatesChangedEvent();
    },

    /**
     * Initializes the already uploaded reports from provided reports
     */
    prefillAlreadyUploadedReports() {
      if (this.referencedReportsForPrefill) {
        for (const key in this.referencedReportsForPrefill) {
          this.storedReports.push({
            reportName: key,
            reference: this.referencedReportsForPrefill[key].reference,
            currency: this.referencedReportsForPrefill[key].currency,
            reportDate: this.referencedReportsForPrefill[key].reportDate,
            isGroupLevel: this.referencedReportsForPrefill[key].isGroupLevel,
          });
        }
        this.emitCertificatesChangedEvent();
      }
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
