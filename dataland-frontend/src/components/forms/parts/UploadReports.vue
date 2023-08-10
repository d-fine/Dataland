<template>
  <div v-if="isEuTaxonomy" class="col-3 p-3 topicLabel">
    <h4 id="uploadReports" class="anchor title">Upload company reports</h4>
    <p>Please upload all relevant reports for this dataset in the PDF format.</p>
  </div>
  <!-- Select company reports -->
  <div :class="isEuTaxonomy ? 'col-9 formFields' : 'formField'">
    <h3 class="mt-0">Select company reports</h3>
    <UploadDocumentsForm ref="uploadDocumentsForm" @reportsUpdated="updateSelectedReports" :name="name" />
  </div>

  <FormKit name="referencedReports" type="group">
    <div class="uploadFormSection">
      <!-- List of company reports to upload -->
      <div
        v-for="reportToUpload of reportsToUpload"
        :key="reportToUpload.file.name"
        :class="isEuTaxonomy ? 'col-9 formFields' : 'col-9 bordered-box p-3 mb-3'"
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
      <div v-if="isEuTaxonomy" class="col-3 p-3 topicLabel">
        <h4 id="uploadReports" class="anchor title">Uploaded company reports</h4>
      </div>
      <div v-else class="col-12">
        <h3 class="mt-0">Uploaded company reports</h3>
      </div>
      <div
        v-for="(storedReport, index) of storedReports"
        :key="storedReport.reportName"
        :class="isEuTaxonomy ? 'col-9 formFields' : 'col-9 bordered-box p-3 mb-3'"
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
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import ReportFormElement from "@/components/forms/parts/ReportFormElement.vue";
import ElementsDialog from "@/components/general/ElementsDialog.vue";
import { ReportToUpload, StoredReport } from "@/utils/FileUploadUtils";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { CompanyReport } from "@clients/backend";
import { ObjectType } from "@/utils/UpdateObjectUtils";

type DuplicatesWithIndexList = { report: ReportToUpload; index: number }[];

export default defineComponent({
  name: "UploadReports",
  inheritAttrs: false,
  inject: {
    injectReferencedReportsForPrefill: {
      from: "referencedReportsForPrefill",
      default: {},
    },
  },
  components: {
    UploadDocumentsForm,
    ReportFormElement,
    PrimeButton,
  },
  emits: ["reportsUpdated"],
  data() {
    return {
      reportsToUpload: [] as ReportToUpload[],
      storedReports: [] as StoredReport[],
      foundDuplicates: [] as ReportToUpload[],
    };
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    referencedReportsForPrefill: {
      type: Object as () => { [key: string]: CompanyReport },
    },
    isEuTaxonomy: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    referencedReportsForPrefill() {
      this.prefillAlreadyUploadedReports();
    },
  },
  computed: {
    namesOfReportsToUpload() {
      return this.reportsToUpload.map((reportToUpload) => reportToUpload.fileNameWithoutSuffix);
    },
    namesOfStoredReports() {
      return this.storedReports.map((storedReport) => storedReport.reportName);
    },
    allReferenceableReportNames(): string[] {
      return this.namesOfReportsToUpload.concat(this.namesOfStoredReports);
    },
  },
  mounted() {
    this.prefillAlreadyUploadedReports();
  },
  methods: {
    /**
     * Emits event when referenceable reports changed
     */
    emitReportsUpdatedEvent() {
      if (this.foundDuplicates?.length) {
        this.openModalToDisplayDuplicateNameError();
      }

      if (this.isEuTaxonomy) {
        this.$emit("reportsUpdated", this.allReferenceableReportNames);
      } else {
        this.$emit("reportsUpdated", this.allReferenceableReportNames, this.reportsToUpload);
      }
    },
    /**
     * Handles selection of a file by the user. First it checks if the file name is already taken.
     * If yes, the selected file is removed again and a popup with an error message is shown.
     * Else the file is added to the reports that shall be uploaded, then the sha256 hashes are calculated
     * and added to the respective files.
     * @param reports the list of all reports currently selected in the file upload
     */
    updateSelectedReports(reports: ReportToUpload[]) {
      this.reportsToUpload = reports;
      const duplicatesWithIndex: DuplicatesWithIndexList = [];

      if (this.areDuplicatesAmongReferenceableReportNames()) {
        const foundExistingRecords = new Set<string>();

        for (let i = 0; i < this.reportsToUpload.length; i++) {
          const currentName = this.reportsToUpload[i].fileNameWithoutSuffix;

          if (foundExistingRecords.has(currentName) || this.namesOfStoredReports.indexOf(currentName) !== -1) {
            duplicatesWithIndex.push({ report: this.reportsToUpload[i], index: i });
          } else {
            foundExistingRecords.add(currentName);
          }
        }

        this.handleReportDuplicates([...duplicatesWithIndex].reverse());
      } else {
        this.emitReportsUpdatedEvent();
      }
    },
    /**
     * Scan list of file names and show modal if duplicate
     * @param duplicatesWithIndex a list of reports to upload
     */
    handleReportDuplicates(duplicatesWithIndex: DuplicatesWithIndexList) {
      const reports = duplicatesWithIndex.map(({ report }) => report);
      const indexes = duplicatesWithIndex.map(({ index }) => index);

      this.foundDuplicates = reports;
      (this.$refs.uploadDocumentsForm.removeDocumentsFromDocumentsToUpload as (indexes: number[]) => void)(indexes);
    },
    /**
     * When the X besides existing reports is clicked this function should be called and
     * removes the corresponding report from the list
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromStoredReports(indexOfFileToRemove: number) {
      this.storedReports.splice(indexOfFileToRemove, 1);
      this.emitReportsUpdatedEvent();
    },

    /**
     * Initializes the already uploaded reports from provided reports
     */
    prefillAlreadyUploadedReports() {
      const sourceOfReferencedReportsForPrefill = (this.referencedReportsForPrefill ??
        this.injectReferencedReportsForPrefill) as ObjectType;

      if (sourceOfReferencedReportsForPrefill) {
        for (const key in sourceOfReferencedReportsForPrefill) {
          const referencedReport = (sourceOfReferencedReportsForPrefill as { [key: string]: CompanyReport })[key];
          this.storedReports.push({
            reportName: key,
            reference: referencedReport.reference,
            currency: referencedReport.currency,
            reportDate: referencedReport.reportDate,
            isGroupLevel: referencedReport.isGroupLevel,
          });
        }
        this.emitReportsUpdatedEvent();
      }
    },

    /**
     * Opens a modal and explains the user that the selected file has a name for which a report already exists.
     * @param nameOfFileThatHasDuplicate contains the file name which caused the error
     */
    openModalToDisplayDuplicateNameError() {
      const duplicateReportNamesList = [
        ...new Set(this.foundDuplicates.map((report: ReportToUpload) => report.fileNameWithoutSuffix)),
      ].join(", ");

      this.$dialog.open(ElementsDialog, {
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
          listOfElementNames: [duplicateReportNamesList],
        },
      });
      this.foundDuplicates = [];
    },

    /**
     * Checks if there is a report name twice in the list of referencable report names
     * @returns a boolean stating if any file name is duplicated among the reference report names
     */
    areDuplicatesAmongReferenceableReportNames(): boolean {
      return this.allReferenceableReportNames.length !== new Set(this.allReferenceableReportNames).size;
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
