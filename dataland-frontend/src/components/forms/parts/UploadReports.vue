<template>
  <div v-if="isEuTaxonomy" class="col-3 p-3 topicLabel">
    <h4 id="uploadReports" class="anchor title">Upload company reports</h4>
    <p>Please upload all relevant reports for this dataset in the PDF format.</p>
  </div>
  <!-- Select company reports -->
  <div :class="isEuTaxonomy ? 'col-9 formFields' : 'formField'">
    <h3 class="mt-0">Select company reports</h3>
    <UploadDocumentsForm ref="uploadDocumentsForm" @documentsChanged="updateSelectedReports" :name="name" />
  </div>

  <FormKit name="referencedReports" type="group">
    <div class="uploadFormSection">
      <!-- List of company reports to upload -->
      <div
        v-for="reportToUpload of reportsToUpload"
        :key="reportToUpload.file.name"
        :class="isEuTaxonomy ? 'col-9 formFields' : 'bordered-box p-3 mb-3'"
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
import { UploadReportsProps } from "@/components/forms/parts/fields/FormFieldProps";
import { CompanyReport } from "@clients/backend";

export default defineComponent({
  name: "UploadReports",
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
  emits: ["documentUpdated"],
  data() {
    return {
      reportsToUpload: [] as ReportToUpload[] | undefined,
      storedReports: [] as StoredReport[],
    };
  },
  props: {
    ...UploadReportsProps,
  },
  watch: {
    referencedReportsForPrefill() {
      this.prefillAlreadyUploadedReports();
    },
  },
  computed: {
    namesOfFilesToUpload() {
      return this.reportsToUpload.map((reportToUpload) => reportToUpload.fileNameWithoutSuffix);
    },
    namesOfStoredReports() {
      return this.storedReports.map((storedReport) => storedReport.reportName);
    },
    allReferenceableReportNames(): string[] {
      return this.namesOfFilesToUpload.concat(this.namesOfStoredReports);
    },
  },
  mounted() {
    this.prefillAlreadyUploadedReports();
  },
  methods: {
    /**
     * Emits event that referenceable files changed
     */
    emitReferenceableReportNamesChangedEvent() {
      if (this.isEuTaxonomy) {
        this.$emit("documentUpdated", this.allReferenceableReportNames);
      } else {
        this.$emit("documentUpdated", this.allReferenceableReportNames, this.reportsToUpload);
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
      if (this.duplicatesAmongReferenceableReportNames()) {
        const foundExistingRecords = [];
        for (let i = 0; i < this.reportsToUpload.length; i++) {
          const currentName = this.reportsToUpload[i].fileNameWithoutSuffix;
          if (
            foundExistingRecords.indexOf(currentName) === -1 &&
            this.namesOfStoredReports.indexOf(currentName) === -1
          ) {
            foundExistingRecords.push(currentName);
          } else {
            this.openModalToDisplayDuplicateNameError(this.reportsToUpload[i].fileNameWithoutSuffix);
            (this.$refs.uploadDocumentsForm.removeDocumentFromDocumentsToUpload as (index: number) => void)(i);
            i--; // We reduce i to recheck the item that will take the place of the deleted item
          }
        }
      } else {
        this.emitReferenceableReportNamesChangedEvent();
      }
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
     * Initializes the already uploaded reports from provided reports
     */
    prefillAlreadyUploadedReports() {
      const sourceOfReferencedReportsForPrefill =
        this.referencedReportsForPrefill ?? this.injectReferencedReportsForPrefill;
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
        this.emitReferenceableReportNamesChangedEvent();
      }
    },

    /**
     * Opens a modal and explains the user that the selected file has a name for which a report already exists.
     * @param nameOfFileThatHasDuplicate contains the file name which caused the error
     */
    openModalToDisplayDuplicateNameError(nameOfFileThatHasDuplicate: string) {
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
          listOfElementNames: [nameOfFileThatHasDuplicate],
        },
      });
    },

    /**
     * Checks if there is a report name twice in the list of referencable report names
     * @returns a boolean stating if any file name is duplicated among the reference report names
     */
    duplicatesAmongReferenceableReportNames(): boolean {
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
