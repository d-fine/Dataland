<template>
  <div v-if="!editMode" class="col-3 p-3 topicLabel">
    <!-- TODO Florian does not get why this is only rendered when editing a dataset, todo check figma -->
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
      :maxFileSize="UPLOAD_MAX_FILE_SIZE_IN_BYTES"
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
              :label="editMode ? 'ADD REPORTS' : 'SELECT REPORTS'"
            />
            <!-- TODO I don't see why this shouldn't be uniform, todo check figma -->
          </div>
        </div>
      </template>
      <template #content="{ files, removeFileCallback }">
        <div v-if="files.length > 0" data-test="uploaded-files">
          <div
            v-for="(file, index) of files"
            :key="file.name + index"
            class="flex w-full align-items-center file-upload-item"
          >
            <span data-test="uploaded-files-title" class="font-semibold flex-1">{{ file.name }}</span>
            <div data-test="uploaded-files-size" class="mx-2 text-black-alpha-50">
              {{ formatBytesUserFriendly(file.size, 3) }}
            </div>
            <PrimeButton
              data-test="uploaded-files-remove"
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
      <div v-for="(file, index) of reportsToUpload" :key="file.name" class="col-9 formFields" data-test="report-info">
        <div v-if="file.nameAlreadyExists === 'true'">
          <div>
            File with name:
            <h3 data-test="file-name-already-exists">{{ file.name.split(".")[0] }}</h3>
            Already exists. Please upload file with different name.
          </div>
        </div>
        <div v-else :data-test="file.name.split('.')[0] + 'ToUploadContainer'">
          <div class="form-field-label">
            <h3 class="mt-0">{{ file.name.split(".")[0] }}</h3>
          </div>
          <ReportFormElement
            :file="file"
            @reporting-date-changed="(date: Date) => { updateReportDateHandler(index, date, reportsToUpload) }"
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
          :file="file"
          @reporting-date-changed="(date: Date) => { updateReportDateHandler(index, date, uploadedReports) }"
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
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import { CompanyReport } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import ReportFormElement from "@/components/forms/parts/ReportFormElement.vue";

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
      UPLOAD_MAX_FILE_SIZE_IN_BYTES,
      reportsToUpload: [] as (CompanyReportUploadModel & File)[],
      uploadedReports: [] as CompanyReportUploadModel[],
      euTaxonomyKpiNameMappings, // TODO doesn't this make this component eutaxonomy-specific?  Now we cannot use it generically anymore, right?
      euTaxonomyKpiInfoMappings,
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
     *
     * @param event full event object containing the files
     * @param event.originalEvent event information
     * @param event.files files
     */
    async handleFilesSelected(event: FileUploadSelectEvent): void {
      this.reportsToUpload = [
        ...this.completeInformationAboutSelectedFileWithAdditionalFields(
          event.files as Record<string, string>[],
          this.uploadedReports
        ),
      ] as (CompanyReportUploadModel & File)[];
      this.reportsToUpload = await Promise.all(
        this.reportsToUpload.map(async (extendedFile) => {
          // TODO this assumes that the hash in the frontend ALWAYS equals the one in the backend!  Can we guarantee that? Should we assume that?
          // TODO I find that fine as long as we test, which we don't.
          extendedFile.reference = await this.calculateSha256HashFromFile(extendedFile);
          return extendedFile;
        })
      );
      this.referenceableFilesChanged();
    },
    /**
     * Remove report from files uploaded
     *
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
     *
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromUploadedReports(indexOfFileToRemove: number) {
      this.uploadedReports.splice(indexOfFileToRemove, 1);
      this.referenceableFilesChanged();
    },

    /**
     * Updates the date of a single report file
     *
     * @param index file to update
     * @param date new date value
     * @param setOfFiles which set of files will be edited
     */
    updateReportDateHandler(index: number, date: Date, setOfFiles: CompanyReportUploadModel[]): void {
      setOfFiles[index].reportDate = getHyphenatedDate(date);
      setOfFiles[index].reportDateAsDate = date;
    },
    /**
     * Uploads the filed that are to be uploaded if they are not already available to dataland
     */
    async uploadFiles() {
      this.checkIfThereAreNoDuplicateReportNames();
      const documentUploadControllerControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise())
      ).getDocumentControllerApi();
      for (const file of this.reportsToUpload) {
        const fileIsAlreadyInStorage = (await documentUploadControllerControllerApi.checkDocument(file.reference)).data
          .documentExists;
        if (!fileIsAlreadyInStorage) {
          await documentUploadControllerControllerApi.postDocument(file); // TODO assure that hash by frontend equals the one from backend
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
            reportDateAsDate: referencedReportsForDataId[key].reportDate
              ? new Date(referencedReportsForDataId[key].reportDate as string)
              : "",
          });
        }
        this.referenceableFilesChanged();
      }
    },
    /**
     * Complete information about selected file with additional fields
     *
     * @param filesThatShouldBeCompleted Files that should be completed
     * @param listOfFilesThatAlreadyExistInReportsInfo List Of Files That Already Exist In Reports Info
     * @returns List of files with additional fields
     */
    completeInformationAboutSelectedFileWithAdditionalFields(
      filesThatShouldBeCompleted: Record<string, string>[],
      listOfFilesThatAlreadyExistInReportsInfo: CompanyReportUploadModel[]
    ): (CompanyReportUploadModel & File)[] {
      return filesThatShouldBeCompleted.map((file) => {
        if (listOfFilesThatAlreadyExistInReportsInfo.some((it) => it.name === file.name.split(".")[0])) {
          file["nameAlreadyExists"] = "true";
        } else {
          file["nameAlreadyExists"] = "false";
          file["reportDate"] = file["reportDate"] ?? "";
          file["reportDateAsDate"] = file["reportDateAsDate"] ?? "";
          file["reference"] = file["reference"] ?? ""; // TODO just an idea: Why don't we add "reference" and "nameAlreadyExists" as fields to the CompanyReportUploadModel ?
        }
        return file as CompanyReportUploadModel & File;
      });
    },

    /**
     * checks if all reports that shall be uploaded do not have the same name as an already uploaded report
     */
    checkIfThereAreNoDuplicateReportNames(): void {
      // TODO deep dive into the function
      const duplicateFileNames = this.reportsToUpload
        .filter((extendedFile) => extendedFile["nameAlreadyExists"] === "true")
        .map((extendedFile) => extendedFile.name);
      if (duplicateFileNames.length >= 1) {
        throw new Error(
          `Some of the reports cannot be uploaded because another report with the same name already exists: ${duplicateFileNames.toString()}`
        );
      }
    },

    /**
     *  calculates the hash from a file
     *
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
     *
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
  reportDateAsDate: string | Date; // TODO somehow this is weird.  It is named "reportDateAsDate", but can be a string type?
  [key: string]: unknown;
}
</script>

// TODO data-test="uploaded-files" is not a very good named marker, since the list it refers to is actually the list of
files to upload!

<style scoped>
.p-button-edit-reports {
  width: 1rem;
  border-radius: 50%;
  height: 1rem;
  padding: 12px;
}
</style>
