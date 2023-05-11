<template>
  <div class="col-3 p-3 topicLabel">
    <h4 id="uploadReports" class="anchor title">Upload company reports</h4>
    <p>Please upload all relevant reports for this dataset in the PDF format.</p>
  </div>
  <!-- Select company reports -->
  <div class="col-9 formFields uploaded-files">
    <h3 class="mt-0">Select company reports</h3>
    <FileUpload
      name="fileUpload"
      ref="fileUpload"
      accept=".pdf"
      @select="handleFilesSelected"
      :maxFileSize="DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES"
      invalidFileSizeMessage="{0}: Invalid file size, file size should be smaller than {1}."
      :auto="false"
    >
      <template #header="{ chooseCallback }">
        <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
          <div class="flex gap-2">
            <PrimeButton
              data-test="upload-files-button"
              @click="chooseCallback()"
              icon="pi pi-upload"
              label="SELECT REPORTS"
            />
          </div>
        </div>
      </template>
      <template #content="{ files, removeFileCallback }">
        <div v-if="files.length > 0" data-test="files-to-upload">
          <div
            v-for="(selectedFile, index) of files"
            :key="selectedFile.name + index"
            class="flex w-full align-items-center file-upload-item"
            :data-test="removeFileTypeExtension(selectedFile.name) + 'FileUploadContainer'"
          >
            <span data-test="files-to-upload-title" class="font-semibold flex-1">{{ selectedFile.name }}</span>
            <div data-test="files-to-upload-size" class="mx-2 text-black-alpha-50">
              {{ formatBytesUserFriendly(selectedFile.size, 1) }}
            </div>
            <PrimeButton
              data-test="files-to-upload-remove"
              icon="pi pi-times"
              @click="removeReportFromReportsToUpload(removeFileCallback, index)"
              class="p-button-rounded"
            />
          </div>
        </div>
      </template>
    </FileUpload>
  </div>
  <FormKit name="referencedReports" type="group">
    <div class="uploadFormSection">
      <!-- List of company reports to upload -->
      <div
        v-for="reportToUpload of reportsToUpload"
        :key="reportToUpload.fileForReport.name"
        class="col-9 formFields"
        data-test="report-to-upload-form"
      >
        <div :data-test="reportToUpload.fileNameWithoutSuffix + 'ToUploadContainer'">
          <div class="form-field-label">
            <h3 class="mt-0">{{ reportToUpload.fileNameWithoutSuffix }}</h3>
          </div>
          <ReportFormElement :name="reportToUpload.fileNameWithoutSuffix" :reference="reportToUpload.reference" />
        </div>
      </div>
    </div>
    <div v-if="storedReports.length > 0" class="uploadFormSection">
      <!-- List of company reports -->
      <div class="col-3 p-3 topicLabel">
        <h4 id="uploadReports" class="anchor title">Uploaded company reports</h4>
      </div>
      <div
        v-for="(storedReport, index) of storedReports"
        :key="storedReport.reportName"
        class="col-9 formFields"
        data-test="report-uploaded-form"
      >
        <div :data-test="storedReport.reportName + 'AlreadyUploadedContainer'" class="form-field-label">
          <div class="flex w-full">
            <h3 class="mt-0">{{ storedReport.reportName }}</h3>
            <PrimeButton
              :data-test="'remove-' + storedReport.reportName"
              @click="removeReportFromStoredReports(index)"
              icon="pi pi-times"
              class="p-button-edit-reports"
            />
          </div>
        </div>
        <ReportFormElement
          :name="storedReport.reportName"
          :report-date="storedReport.reportDate"
          :reference="storedReport.reference"
        />
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import FileUpload, { FileUploadSelectEvent } from "primevue/fileupload";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import { CompanyReport } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import ReportFormElement from "@/components/forms/parts/ReportFormElement.vue";
import FilesDialog from "@/components/general/ElementsDialog.vue";

export default defineComponent({
  name: "UploadReports",
  components: {
    ReportFormElement,
    PrimeButton,
    FileUpload,
  },
  emits: ["referenceableReportNamesChanged"],
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
      reportsToUpload: [] as ReportToUpload[],
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
      const namesOfFilesToUpload = this.reportsToUpload.map((reportToUpload) => reportToUpload.fileNameWithoutSuffix);
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
    /**
     * Emits event that referenceable files changed
     */
    emitReferenceableReportNamesChangedEvent() {
      this.$emit("referenceableReportNamesChanged", this.allReferenceableReportNames);
    },
    /**
     * Handles selection of a file by the user. First it checks if the file name is already taken.
     * If yes, the selected file is removed again and a popup with an error message is shown.
     * Else the file is added to the reports that shall be uploaded, then the sha256 hashes are calculated and added
     * to the respective files.
     * @param event full event object containing the files
     * @param event.files files
     */
    async handleFilesSelected(event: FileUploadSelectEvent): void {
      const selectedFilesByUser = event.files as File[];
      if (this.wasThereNoNewFileAdded(selectedFilesByUser)) {
        return;
      }
      const indexOfLastSelectedFile = selectedFilesByUser.length - 1;
      const lastSelectedFile = selectedFilesByUser[indexOfLastSelectedFile];
      if (this.isFileNameAlreadyExistingAmongReferenceableReportNames(lastSelectedFile.name)) {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call
        this.$refs.fileUpload.remove(indexOfLastSelectedFile);
        this.openModalToDisplayDuplicateNameError(lastSelectedFile.name);
      } else {
        const reportToUpload = { fileForReport: lastSelectedFile } as ReportToUpload;
        reportToUpload.reference = await this.calculateSha256HashFromFile(reportToUpload.fileForReport);
        reportToUpload.fileNameWithoutSuffix = this.removeFileTypeExtension(reportToUpload.fileForReport.name);
        this.reportsToUpload.push(reportToUpload);
        this.emitReferenceableReportNamesChangedEvent();
      }
    },
    /**
     * Checks if there was actually a file added by the user that was not filtered
     * out by the FileUpload component
     * @param filesSelectedByUser the files currently selected by the user
     * @returns false if there was actually a file added by the user
     */
    wasThereNoNewFileAdded(filesSelectedByUser: File[]) {
      // TODO I suggest renaming this, since it was a little confusing for me. e.g. "isSelectedFileBeingActuallyAdded" true = yes  false = no
      return filesSelectedByUser.length == this.reportsToUpload.length;
    },
    /**
     * Remove report from files uploaded
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param indexOfFileToRemove index number of the file to remove
     */
    removeReportFromReportsToUpload(fileRemoveCallback: (x: number) => void, indexOfFileToRemove: number) {
      fileRemoveCallback(indexOfFileToRemove);
      this.reportsToUpload.splice(indexOfFileToRemove, 1);
      this.emitReferenceableReportNamesChangedEvent();
    },

    /**
     * When the X besides existing reports is clicked this function should be called and
     * removes the corresponding report from the list
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromStoredReports(indexOfFileToRemove: number) {
      this.storedReports.splice(indexOfFileToRemove, 1);
      this.emitReferenceableReportNamesChangedEvent();
    },

    /**
     * Uploads the filed that are to be uploaded if they are not already available to dataland
     */
    async uploadFiles() {
      const documentUploadControllerControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise())
      ).getDocumentControllerApi();
      for (const reportToUpload of this.reportsToUpload) {
        const fileIsAlreadyInStorage = (
          await documentUploadControllerControllerApi.checkDocument(reportToUpload.reference)
        ).data.documentExists;
        if (!fileIsAlreadyInStorage) {
          const backendComputedHash = (
            await documentUploadControllerControllerApi.postDocument(reportToUpload.fileForReport)
          ).data.documentId;
          if (reportToUpload.reference !== backendComputedHash) {
            throw Error("Locally computed document hash does not concede with the one received by the upload request!");
          }
        }
      }
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
        this.emitReferenceableReportNamesChangedEvent();
      }
    },

    /**
     * Opens a modal and explains the user that the selected file has a name for which a report already exists.
     * @param nameOfFileThatHasDuplicate contains the file name which caused the error
     */
    openModalToDisplayDuplicateNameError(nameOfFileThatHasDuplicate: string) {
      this.$dialog.open(FilesDialog, {
        props: {
          modal: true,
          closable: true,
          dismissableMask: true,
          header: "Invalid File Selection",
        },
        data: {
          message:
            "The following file cannot be selected because a report with its name is already selected " +
            "for upload or even already uploaded:",
          listOfElementNames: [nameOfFileThatHasDuplicate],
        },
      });
    },

    /**
     * Checks for a single file name if it already occurs among the referenceable report names
     * @param fullFileName is the full file name with its prefix that should be checked
     * @returns a boolean stating if the file name is among the referenceable report names
     */
    isFileNameAlreadyExistingAmongReferenceableReportNames(fullFileName: string): boolean {
      const fileNameWithoutSuffix = this.removeFileTypeExtension(fullFileName);
      return this.allReferenceableReportNames.some((reportName) => reportName === fileNameWithoutSuffix);
    },

    /**
     *  calculates the hash from a file
     * @param [file] the file to calculate the hash for
     * @returns a promise of the hash as string
     */
    async calculateSha256HashFromFile(file: File): Promise<string> {
      const buffer = await file.arrayBuffer();
      const hashBuffer = await crypto.subtle.digest("SHA-256", buffer);
      return this.toHex(hashBuffer);
    },
    /**
     *  helper to encode a hash of type buffer in hex
     * @param [buffer] the buffer to encode in hex
     * @returns  the array as string, hex encoded
     */
    toHex(buffer: ArrayBuffer): string {
      const array = Array.from(new Uint8Array(buffer)); // convert buffer to byte array
      return array.map((b) => b.toString(16).padStart(2, "0")).join(""); // convert bytes to hex string
    },

    /**
     * Removes the file extension after the last dot of the filename.
     * E.g. someFileName.with.dots.pdf will be converted to someFileName.with.dots
     * @param fileName the file name
     * @returns the file name without the file extension after the last dot
     */
    removeFileTypeExtension(fileName: string): string {
      return fileName.split(".").slice(0, -1).join(".");
    },
  },
});
interface StoredReport extends CompanyReport {
  reportName: string;
}
interface ReportToUpload extends CompanyReport {
  fileForReport: File;
  fileNameWithoutSuffix: string;
}
</script>

<style scoped>
.p-button-edit-reports {
  width: 1rem;
  border-radius: 50%;
  height: 1rem;
  padding: 12px;
}
</style>
