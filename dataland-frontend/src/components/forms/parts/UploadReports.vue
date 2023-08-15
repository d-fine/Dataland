<template>
  <div v-if="isEuTaxonomy" class="col-3 p-3 topicLabel">
    <h4 id="uploadReports" class="anchor title">Upload company reports</h4>
    <p>Please upload all relevant reports for this dataset in the PDF format.</p>
  </div>
  <!-- Select company reports -->
  <div :class="isEuTaxonomy ? 'col-9 formFields' : 'formField'">
    <h3 class="mt-0">Select company reports</h3>
    <UploadDocumentsForm
      ref="uploadDocumentsForm"
      @updatedDocumentsSelectedForUpload="handleUpdatedDocumentsSelectedForUpload"
      :name="name"
    />
  </div>

  <FormKit name="referencedReports" type="group">
    <div class="uploadFormSection">
      <!-- List of company reports to upload -->
      <div
        v-for="documentToUpload of documentsToUpload"
        :key="documentToUpload.file.name"
        :class="isEuTaxonomy ? 'col-9 formFields' : 'col-9 bordered-box p-3 mb-3'"
        data-test="report-to-upload-form"
      >
        <div :data-test="documentToUpload.fileNameWithoutSuffix + 'ToUploadContainer'">
          <div class="form-field-label">
            <h3 class="mt-0">{{ documentToUpload.fileNameWithoutSuffix }}</h3>
          </div>
          <ReportFormElement :name="documentToUpload.fileNameWithoutSuffix" :reference="documentToUpload.reference" />
        </div>
      </div>
    </div>
    <div v-if="alreadyStoredReports.length > 0" class="uploadFormSection">
      <!-- List of company reports -->
      <div v-if="isEuTaxonomy" class="col-3 p-3 topicLabel">
        <h4 id="uploadReports" class="anchor title">Uploaded company reports</h4>
      </div>
      <div v-else class="col-12">
        <h3 class="mt-0">Uploaded company reports</h3>
      </div>
      <div
        v-for="(storedReport, index) of alreadyStoredReports"
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
import { DocumentToUpload, ReportToUpload, StoredReport } from "@/utils/FileUploadUtils";
import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { CompanyReport } from "@clients/backend";
import { ObjectType } from "@/utils/UpdateObjectUtils";

type DuplicateWithIndex = { document: DocumentToUpload; index: number };

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
      documentsToUpload: [] as DocumentToUpload[],
      alreadyStoredReports: [] as StoredReport[],
      namesOfDuplicatesAmongDocumentsToUpload: [] as string[],
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
    namesOfDocumentsToUpload() {
      return this.documentsToUpload.map((documentToUpload) => documentToUpload.fileNameWithoutSuffix);
    },
    namesOfStoredReports() {
      return this.alreadyStoredReports.map((storedReport) => storedReport.reportName);
    },
    allReferenceableReportNames(): string[] {
      return this.namesOfDocumentsToUpload.concat(this.namesOfStoredReports);
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
      if (this.namesOfDuplicatesAmongDocumentsToUpload?.length) {
        this.openModalToDisplayDuplicateNameError();
      }

      if (this.isEuTaxonomy) {
        this.$emit("reportsUpdated", this.allReferenceableReportNames);
      } else {
        this.$emit("reportsUpdated", this.allReferenceableReportNames, this.documentsToUpload);
      }
    },
    /**
     * Handles selection of a file by the user. If duplicates are found in the selection, this is handled.
     * At the end an event is emitted reflecting that the referenceable reports have updated.
     * @param documents the list of all documents for the upload, determined by the selection in the file uploader
     */
    handleUpdatedDocumentsSelectedForUpload(documents: DocumentToUpload[]) {
      this.documentsToUpload = documents;
      const duplicatesWithIndex: DuplicateWithIndex[] = [];

      if (this.areDuplicatesAmongReferenceableReportNames()) {
        const foundExistingRecords = new Set<string>();

        for (let i = 0; i < this.documentsToUpload.length; i++) {
          const currentName = this.documentsToUpload[i].fileNameWithoutSuffix;

          if (foundExistingRecords.has(currentName) || this.namesOfStoredReports.indexOf(currentName) !== -1) {
            duplicatesWithIndex.push({ document: this.documentsToUpload[i], index: i });
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
     * This handles duplicates in the file selection by removing them from the file selection.
     * @param duplicatesWithIndex duplicate documents together with their indexes in the file selection list
     */
    handleReportDuplicates(duplicatesWithIndex: DuplicateWithIndex[]) {
      const documents: DocumentToUpload[] = duplicatesWithIndex.map(({ document }) => document);
      const indexes: number[] = duplicatesWithIndex.map(({ index }) => index);

      this.namesOfDuplicatesAmongDocumentsToUpload = documents.map((document) => document.fileNameWithoutSuffix);
      (this.$refs.uploadDocumentsForm.removeDocumentsFromDocumentsToUpload as (indexes: number[]) => void)(indexes);
    },
    /**
     * When the X besides existing reports is clicked this function should be called and
     * remove the corresponding selected file from the list of files selected for the upload.
     * @param indexOfFileToRemove Index of the file that shall be removed from the users file selection
     */
    removeReportFromStoredReports(indexOfFileToRemove: number) {
      this.alreadyStoredReports.splice(indexOfFileToRemove, 1);
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
          this.alreadyStoredReports.push({
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
     * Opens a modal and explains the user that selected files have names for which a report already exists.
     */
    openModalToDisplayDuplicateNameError() {
      const duplicatesSelectedByUser = [...new Set(this.namesOfDuplicatesAmongDocumentsToUpload)].join(", ");

      this.$dialog.open(ElementsDialog, {
        props: {
          modal: true,
          closable: true,
          dismissableMask: true,
          header: "Invalid File Selection",
        },
        data: {
          message:
            "The following file(s) cannot be selected because a report with its name is already selected " +
            "for upload or even already uploaded:",
          listOfElementNames: [duplicatesSelectedByUser],
        },
      });
      this.namesOfDuplicatesAmongDocumentsToUpload = [];
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
