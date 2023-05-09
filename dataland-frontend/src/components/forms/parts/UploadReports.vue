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
            v-for="(file, index) of files"
            :key="file.name + index"
            class="flex w-full align-items-center file-upload-item"
          >
            <span data-test="files-to-upload-title" class="font-semibold flex-1">{{ file.name }}</span>
            <div data-test="files-to-upload-size" class="mx-2 text-black-alpha-50">
              {{ formatBytesUserFriendly(file.size, 1) }}
            </div>
            <PrimeButton
              data-test="files-to-upload-remove"
              icon="pi pi-times"
              @click="removeReportFromFilesToUpload(removeFileCallback, index)"
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
        v-for="(report, index) of reportsToUpload"
        :key="report.name"
        class="col-9 formFields"
        data-test="report-info"
      >
        <div :data-test="report.name.split('.')[0] + 'ToUploadContainer'">
          <div class="form-field-label">
            <h3 class="mt-0">{{ report.name.split(".")[0] }}</h3>
          </div>
          <ReportFormElement
            :name="report.name.split('.')[0]"
            :report-date="report.reportDate"
            :reference="report.reference"
            @reporting-date-changed="(date: Date) => { updateReportDateHandler(date, index, reportsToUpload) }"
          />
        </div>
      </div>
    </div>
    <div v-if="editMode" class="uploadFormSection">
      <!-- List of company reports -->
      <div v-if="uploadedReports.length > 0" class="col-3 p-3 topicLabel">
        <h4 id="uploadReports" class="anchor title">Uploaded company reports</h4>
      </div>
      <div v-for="(file, index) of uploadedReports" :key="file.name" class="col-9 formFields">
        <div :data-test="file.name.split('.')[0] + 'AlreadyUploadedContainer'" class="form-field-label">
          <div class="flex w-full">
            <h3 class="mt-0">{{ file.name.split(".")[0] }}</h3>
            <PrimeButton
              :data-test="'remove-' + file.name.split('.')[0]"
              @click="removeReportFromUploadedReports(index)"
              icon="pi pi-times"
              class="p-button-edit-reports"
            />
          </div>
        </div>
        <ReportFormElement
          :name="file.name.split('.')[0]"
          :report-date="file.reportDate"
          :reference="file.reference"
          @reporting-date-changed="(date: Date) => { updateReportDateHandler(date, index, uploadedReports) }"
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
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import ReportFormElement from "@/components/forms/parts/ReportFormElement.vue";
import FilesDialog from "@/components/general/ElementsDialog.vue";

export default defineComponent({
  name: "UploadReports",
  components: {
    ReportFormElement,
    PrimeButton,
    FileUpload,
  },
  emits: ["referenceableFilesChanged"],
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
      reportsToUpload: [] as (CompanyReportUploadModel & File)[],
      uploadedReports: [] as CompanyReportUploadModel[],
    };
  },
  props: {
    editMode: {
      type: Boolean,
    },
    dataset: {
      type: Object as () => { referencedReports: { [key: string]: CompanyReport } },
    },
  },
  computed: {
    allReferenceableReportsFilenames(): string[] {
      return (this.reportsToUpload as CompanyReportUploadModel[])
        .concat(this.uploadedReports)
        .map((it) => it.name.split(".")[0]);
    },
  },
  watch: {
    dataset() {
      this.getExistingReports();
    },
  },
  mounted() {
    this.getExistingReports();
  },
  methods: {
    /**
     * Emits event that referenceable files changed
     */
    emitRreferenceableFilesChangedEvent() {
      this.$emit("referenceableFilesChanged", this.allReferenceableReportsFilenames);
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
      const indexOfLastSelectedFile = selectedFilesByUser.length - 1;
      const lastSelectedFile = selectedFilesByUser[indexOfLastSelectedFile];
      if (this.isFileNameAlreadyExistingForAnUploadedOrSelectedReport(lastSelectedFile.name)) {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call
        this.$refs.fileUpload.remove(indexOfLastSelectedFile);
        this.openModalToDisplayDuplicateNameError(lastSelectedFile.name);
      } else {
        const lastSelectedFileHash = await this.calculateSha256HashFromFile(lastSelectedFile);
        const extendedLastSelectedFile = lastSelectedFile as CompanyReportUploadModel & File;
        extendedLastSelectedFile.reference = lastSelectedFileHash;
        this.reportsToUpload.push(extendedLastSelectedFile);
        this.emitRreferenceableFilesChangedEvent();
      }
    },
    /**
     * Remove report from files uploaded
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param indexOfFileToRemove index number of the file to remove
     */
    removeReportFromFilesToUpload(fileRemoveCallback: (x: number) => void, indexOfFileToRemove: number) {
      fileRemoveCallback(indexOfFileToRemove);
      this.reportsToUpload.splice(indexOfFileToRemove, 1);
      this.emitRreferenceableFilesChangedEvent();
    },

    /**
     * When the X besides existing reports is clicked this function should be called and
     * removes the corresponding report from the list
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromUploadedReports(indexOfFileToRemove: number) {
      this.uploadedReports.splice(indexOfFileToRemove, 1);
      this.emitRreferenceableFilesChangedEvent();
    },

    /**
     * Updates the date of a single report file
     * @param newDate new date value
     * @param index file to update
     * @param containingReports which set of files will be edited
     */
    updateReportDateHandler(newDate: Date, index: number, containingReports: CompanyReportUploadModel[]): void {
      containingReports[index].reportDate = getHyphenatedDate(newDate); // TODO probably not even needed
    },
    /**
     * Uploads the filed that are to be uploaded if they are not already available to dataland
     */
    async uploadFiles() {
      const documentUploadControllerControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise())
      ).getDocumentControllerApi();
      for (const file of this.reportsToUpload) {
        const fileIsAlreadyInStorage = (await documentUploadControllerControllerApi.checkDocument(file.reference)).data
          .documentExists;
        if (!fileIsAlreadyInStorage) {
          const backendComputedHash = (await documentUploadControllerControllerApi.postDocument(file)).data.documentId;
          if (file.reference !== backendComputedHash) {
            throw Error("Locally computed document hash does not concede with the one received by the upload request!");
          }
        }
      }
    },
    /**
     * Initializes the already uploaded reports from a dataset
     */
    getExistingReports() {
      if (this.dataset?.referencedReports) {
        const referencedReportsForDataId = this.dataset.referencedReports;
        for (const key in referencedReportsForDataId) {
          this.uploadedReports.push({
            name: key,
            reference: referencedReportsForDataId[key].reference,
            currency: referencedReportsForDataId[key].currency,
            reportDate: referencedReportsForDataId[key].reportDate,
            isGroupLevel: referencedReportsForDataId[key].isGroupLevel,
          });
        }
        this.emitRreferenceableFilesChangedEvent();
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
     * Checks for a single file name if it already occurs among the files that shall be uploaded.
     * @param fullFileName is the full file name with its prefix that should be checked
     * @returns a boolean stating if the file name is among the files that shall be uploaded or not
     */
    isFileNameAlreadyExistingForAnUploadedOrSelectedReport(fullFileName: string): boolean {
      const fileNameWithoutSuffix = fullFileName.split(".")[0];
      return (
        this.uploadedReports.some((uploadedReport) => uploadedReport.name === fileNameWithoutSuffix) ||
        this.reportsToUpload.some((reportToUpload) => reportToUpload.name === fullFileName)
      );
    },

    /**
     *  calculates the hash from a file
     * @param [file] the file to calculate the hash from
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
  }, // TODO investigate if multiple "." break our upload
});
interface CompanyReportUploadModel extends CompanyReport {
  name: string;
  reportDate: string; // TODO probably not even needed
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
