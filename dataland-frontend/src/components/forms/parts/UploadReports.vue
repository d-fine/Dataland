<template>
  <div :data-test="`upload-reports-${name}`" class="display-contents">
    <div v-if="isMountedForEuTaxoFinancialsUploadPage" class="col-3 p-3 topicLabel">
      <h4 class="anchor title">Upload company reports</h4>
      <p>Please upload all relevant reports for this dataset in the PDF format.</p>
    </div>
    <!-- Select company reports -->
    <div :class="isMountedForEuTaxoFinancialsUploadPage ? 'col-9 formFields' : 'formField'">
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
          :class="isMountedForEuTaxoFinancialsUploadPage ? 'col-9 formFields' : 'col-9 bordered-box p-3 mb-3'"
          data-test="report-to-upload-form"
        >
          <div :data-test="documentToUpload.fileNameWithoutSuffix + 'ToUploadContainer'">
            <div class="form-field-label">
              <h3 class="mt-0">{{ documentToUpload.fileNameWithoutSuffix }}</h3>
            </div>
            <ReportFormElement
              :name="documentToUpload.fileNameWithoutSuffix"
              :fileReference="documentToUpload.fileReference"
            />
          </div>
        </div>
      </div>
      <div v-if="alreadyStoredReports.length > 0" class="uploadFormSection">
        <!-- List of company reports -->
        <div v-if="isMountedForEuTaxoFinancialsUploadPage" class="col-3 p-3 topicLabel">
          <h4 class="anchor title">Uploaded company reports</h4>
        </div>
        <div v-else class="col-12">
          <h3 class="mt-0">Uploaded company reports</h3>
        </div>
        <div
          v-for="(storedReport, index) of alreadyStoredReports"
          :key="storedReport.fileName"
          :class="isMountedForEuTaxoFinancialsUploadPage ? 'col-9 formFields' : 'col-9 bordered-box p-3 mb-3'"
          data-test="report-uploaded-form"
        >
          <div :data-test="storedReport.fileName + 'AlreadyUploadedContainer'" class="form-field-label">
            <div class="flex w-full">
              <h3 class="mt-0">{{ storedReport.fileName }}</h3>
              <PrimeButton
                :data-test="'remove-' + storedReport.fileName"
                @click="removeReportFromStoredReports(index)"
                icon="pi pi-times"
                class="p-button-edit-reports"
              />
            </div>
          </div>
          <ReportFormElement
            :name="storedReport.fileName"
            :publication-date="storedReport.publicationDate"
            :fileReference="storedReport.fileReference"
          />
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import PrimeButton from 'primevue/button';
import ReportFormElement from '@/components/forms/parts/ReportFormElement.vue';
import InvalidFileSelectionDialog from '@/components/general/InvalidFileSelectionDialog.vue';
import { calculateReferenceableFiles, type DocumentToUpload, type StoredReport } from '@/utils/FileUploadUtils';
import UploadDocumentsForm from '@/components/forms/parts/elements/basic/UploadDocumentsForm.vue';
import { type CompanyReport } from '@clients/backend';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { REGEX_FOR_FILE_NAMES } from '@/utils/Constants';

enum FileNameInvalidityReason {
  Duplicate = 'Duplicate',
  ForbiddenCharacter = 'ForbiddenCharacter',
}
type NameIndexAndReasonOfInvalidFile = {
  fileName: string;
  index: number;
  invalidityReason: FileNameInvalidityReason;
};

export default defineComponent({
  name: 'UploadReports',
  inheritAttrs: false,
  inject: {
    injectReferencedReportsForPrefill: {
      from: 'referencedReportsForPrefill',
      default: {},
    },
  },
  components: {
    UploadDocumentsForm,
    ReportFormElement,
    PrimeButton,
  },
  emits: ['reportsUpdated'],
  data() {
    return {
      documentsToUpload: [] as DocumentToUpload[],
      alreadyStoredReports: [] as StoredReport[],
      namesInFileSelectionThatAreAlreadyTakenByOtherReports: [] as string[],
      namesInFileSelectionWithForbiddenCharacters: [] as string[],
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
    isMountedForEuTaxoFinancialsUploadPage: {
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
    namesAndReferencesOfDocumentsToUpload(): ObjectType {
      return calculateReferenceableFiles(this.documentsToUpload);
    },

    namesAndReferencesOfStoredReports(): ObjectType {
      return calculateReferenceableFiles(this.alreadyStoredReports);
    },
    allReferenceableReportNamesAndReferences(): ObjectType {
      return { ...this.namesAndReferencesOfDocumentsToUpload, ...this.namesAndReferencesOfStoredReports };
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
      if (
        this.namesInFileSelectionThatAreAlreadyTakenByOtherReports?.length ||
        this.namesInFileSelectionWithForbiddenCharacters?.length
      ) {
        this.openModalToDisplayNameErrorsInFileSelectionByUser();
      }
      this.$emit('reportsUpdated', this.allReferenceableReportNamesAndReferences, this.documentsToUpload);
    },
    /**
     * Handles selection of files by the user. If invalid file names are found in the selection, this is handled.
     * File names are invalid if they contain forbidden characters, or if they already exist either in the current
     * file selection, or among the already uploaded reports (given that the user is in EDIT mode).
     * At the end an event is emitted reflecting that the referenceable reports have updated.
     * @param selectedDocumentsForUpload the list of all selectedDocumentsForUpload for the upload, determined by the selection in the file uploader
     */
    handleUpdatedDocumentsSelectedForUpload(selectedDocumentsForUpload: DocumentToUpload[]) {
      this.documentsToUpload = selectedDocumentsForUpload;

      const nameIndexAndReasonOfInvalidFiles: NameIndexAndReasonOfInvalidFile[] = [];
      const existingFileNamesCollector = new Set<string>();

      for (let i = 0; i < this.documentsToUpload.length; i++) {
        const fileName = this.documentsToUpload[i].fileNameWithoutSuffix;

        if (this.hasFileNameForbiddenCharacter(fileName)) {
          nameIndexAndReasonOfInvalidFiles.push({
            fileName: fileName,
            index: i,
            invalidityReason: FileNameInvalidityReason.ForbiddenCharacter,
          });
        } else if (
          existingFileNamesCollector.has(fileName) ||
          Object.keys(this.namesAndReferencesOfStoredReports).indexOf(fileName) !== -1
        ) {
          nameIndexAndReasonOfInvalidFiles.push({
            fileName: fileName,
            index: i,
            invalidityReason: FileNameInvalidityReason.Duplicate,
          });
        } else {
          existingFileNamesCollector.add(fileName);
        }
      }

      if (nameIndexAndReasonOfInvalidFiles.length > 0) {
        this.handleFilesWithInvalidNames([...nameIndexAndReasonOfInvalidFiles].reverse());
      } else {
        this.emitReportsUpdatedEvent();
      }
    },
    /**
     * Handles invalid file names in the file selection by removing those files from the file selection.
     * @param nameIndexAndReasonOfInvalidFiles invalid file names together with their indexes in the file selection
     * list and the reason for their invalidities
     */
    handleFilesWithInvalidNames(nameIndexAndReasonOfInvalidFiles: NameIndexAndReasonOfInvalidFile[]) {
      this.namesInFileSelectionThatAreAlreadyTakenByOtherReports = nameIndexAndReasonOfInvalidFiles
        .filter((it) => it.invalidityReason === FileNameInvalidityReason.Duplicate)
        .map((it) => it.fileName);
      this.namesInFileSelectionWithForbiddenCharacters = nameIndexAndReasonOfInvalidFiles
        .filter((it) => it.invalidityReason === FileNameInvalidityReason.ForbiddenCharacter)
        .map((it) => it.fileName);
      const indexesOfInvalidFileNames = nameIndexAndReasonOfInvalidFiles.map(
        (fileNameWithIndexAndReason) => fileNameWithIndexAndReason.index
      );
      (this.$refs.uploadDocumentsForm.removeDocumentsFromDocumentsToUpload as (indexes: number[]) => void)(
        indexesOfInvalidFileNames
      );
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
            fileName: key,
            fileReference: referencedReport.fileReference,
            publicationDate: referencedReport.publicationDate,
          });
        }
        this.emitReportsUpdatedEvent();
      }
    },

    /**
     * Opens a modal and explains the user that (some) selected files have invalid names and cannot be selected.
     */
    openModalToDisplayNameErrorsInFileSelectionByUser() {
      this.$dialog.open(InvalidFileSelectionDialog, {
        props: {
          modal: true,
          closable: true,
          dismissableMask: true,
          header: 'Files cannot be uploaded',
        },
        data: {
          duplicateNamesJoinedString: this.namesInFileSelectionThatAreAlreadyTakenByOtherReports.join(', '),
          fileNamesWithCharacterViolationsJoinedString: this.namesInFileSelectionWithForbiddenCharacters.join(', '),
        },
      });
      this.namesInFileSelectionThatAreAlreadyTakenByOtherReports = [];
      this.namesInFileSelectionWithForbiddenCharacters = [];
    },

    /**
     * Checks if a file has a name which contains at least one forbidden character.
     * @param fileName to check
     * @returns a boolean stating the result of that check
     */
    hasFileNameForbiddenCharacter(fileName: string) {
      return !REGEX_FOR_FILE_NAMES.test(fileName);
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
