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
      :multiple="true"
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
            <div v-if="!isFileNameAlreadyExistingInUploadedReports(file.name)">
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
import FilesDialog from "@/components/general/FilesDialog.vue";

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
    referenceableFilesChanged() {
      this.$emit("referenceableFilesChanged", this.allReferenceableReportsFilenames);
    },
    /**
     * Add files to object reportsToUpload
     * @param event full event object containing the files
     * @param event.originalEvent event information
     * @param event.files files
     */
    async handleFilesSelected(event: FileUploadSelectEvent): void {
      const selectedFilesByUser = event.files as File[];
      const selectedFilesByUserWithoutDuplicates = this.filterListToKeepOnlyNonDuplicateFiles(
        selectedFilesByUser
      ) as (CompanyReportUploadModel & File)[];
      this.reportsToUpload = [
        ...this.completeInformationAboutSelectedFileWithAdditionalFields(selectedFilesByUserWithoutDuplicates),
      ] as (CompanyReportUploadModel & File)[];
      this.reportsToUpload = await Promise.all(
        this.reportsToUpload.map(async (extendedFile) => {
          extendedFile.reference = await this.calculateSha256HashFromFile(extendedFile);
          return extendedFile;
        })
      );
      this.referenceableFilesChanged();
    },
    /**
     * Remove report from files uploaded
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param indexOfFileToRemove index number of the file to remove
     */
    removeReportFromFilesToUpload(fileRemoveCallback: (x: number) => void, indexOfFileToRemove: number) {
      fileRemoveCallback(indexOfFileToRemove);
      this.reportsToUpload.splice(indexOfFileToRemove, 1);
      this.referenceableFilesChanged();
    },

    /**
     * When the X besides existing reports is clicked this function should be called and
     * removes the corresponding report from the list
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromUploadedReports(indexOfFileToRemove: number) {
      this.uploadedReports.splice(indexOfFileToRemove, 1);
      this.referenceableFilesChanged();
    },

    /**
     * Updates the date of a single report file
     * @param newDate new date value
     * @param index file to update
     * @param containingReports which set of files will be edited
     */
    updateReportDateHandler(newDate: Date, index: number, containingReports: CompanyReportUploadModel[]): void {
      containingReports[index].reportDate = getHyphenatedDate(newDate);
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
            throw Error("Locally computed document hash does not coincede with the on received by the upload request!");
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
        this.referenceableFilesChanged();
      }
    },

    /**
     * Filters files out if their names already exist among the already uploaded reports for the current dataset.
     * @param listOfSelectedFiles is the list to filter
     * @returns the filtered list.
     */
    filterListToKeepOnlyNonDuplicateFiles(listOfSelectedFiles: File[]): File[] {
      const listOfSelectedFilesWithoutDuplicates: File[] = [];
      const listOfDuplicates: File[] = [];
      listOfSelectedFiles.forEach((selectedFile) => {
        if (this.isFileNameAlreadyExistingInUploadedReports(selectedFile.name)) {
          listOfDuplicates.push(selectedFile);
        } else {
          listOfSelectedFilesWithoutDuplicates.push(selectedFile);
        }
      });
      const strings = listOfDuplicates.map((file) => file.name);
      if (listOfDuplicates.length > 0) {
        this.$dialog.open(FilesDialog, {
          props: {
            modal: true,
            closable: true,
          },
          data: {
            message: "The following files cannot be uploaded because reports with their names already exist:",
            listOfFileNames: strings,
          },
        });
      }
      return listOfSelectedFilesWithoutDuplicates;
    },

    /**
     * Checks for a single file name if it already occurs among the files that shall be uploaded.
     * @param fullFileName is the full file name with its prefix that should be checked
     * @returns a boolean stating if the file name is among the files that shall be uploaded or not
     */
    isFileNameAlreadyExistingInUploadedReports(fullFileName: string): boolean {
      const fileNameWithoutSuffix = fullFileName.split(".")[0];
      return this.uploadedReports.some((uploadedReport) => uploadedReport.name === fileNameWithoutSuffix);
    },

    /**
     * Complete information about selected file with additional fields
     * @param reportsThatShouldBeCompleted Files that should be completed
     * @returns List of files with additional fields
     */
    completeInformationAboutSelectedFileWithAdditionalFields(
      reportsThatShouldBeCompleted: (CompanyReportUploadModel & File)[]
    ): (CompanyReportUploadModel & File)[] {
      return reportsThatShouldBeCompleted.map((reportToComplete) => {
        const fileNameOfReportToComplete = reportToComplete.name.split(".")[0];
        if (!this.uploadedReports.some((report) => report.name === fileNameOfReportToComplete)) {
          reportToComplete.reportDate = reportToComplete.reportDate ?? "";
          reportToComplete.reference = reportToComplete.reference ?? "";
        }
        return reportToComplete;
      });
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
  },
});
interface CompanyReportUploadModel extends CompanyReport {
  name: string;
  reportDate: string;
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
