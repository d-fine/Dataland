<template>
  <PrimeDialog
    v-model:visible="isModalVisible"
    style="text-align: center; width: 20%"
    header="Download dataset"
    :show-header="true"
    :closable="true"
    :dismissable-mask="true"
    :modal="true"
    :close-on-escape="true"
    data-test="downloadModal"
    @after-hide="closeDialog"
  >
    <FormKit type="form" class="formkit-wrapper" :actions="false">
      <label for="reportingYearSelector">
        <b style="margin-bottom: 8px; font-weight: normal">Reporting year</b>
      </label>
      <FormKit
        v-model="selectedReportingPeriod"
        type="select"
        name="reportingYearSelector"
        data-test="reportingYearSelector"
        :options="reportingPeriods"
        placeholder="Select a reporting year"
      />
      <p v-show="showReportingPeriodError" class="text-danger text-xs" data-test="reportingYearError">
        Please select a reporting period.
      </p>
      <label for="formatSelector">
        <b style="margin-bottom: 8px; margin-top: 5px; font-weight: normal">File format</b>
      </label>
      <FormKit
        v-model="selectedFileFormat"
        type="select"
        name="formatSelector"
        data-test="formatSelector"
        :options="fileTypeSelectionOptions"
        placeholder="Select a file format"
      />
      <p v-show="showFileFormatError" class="text-danger text-xs" data-test="fileFormatError">
        Please select a file format.
      </p>
    </FormKit>

    <div>
      <PrimeButton
        data-test="downloadDataButtonInModal"
        @click="onDownloadButtonClick()"
        style="width: 100%; justify-content: center"
        label="DOWNLOAD"
        class="d-letters"
      >
      </PrimeButton>
    </div>
  </PrimeDialog>
</template>

<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import { ExportFileTypes } from '@/types/ExportFileTypes.ts';

export default defineComponent({
  components: { PrimeDialog, PrimeButton },
  name: 'DownloadDatasetModal',
  props: {
    isDownloadModalOpen: {
      type: Boolean,
      required: false,
      default: false,
    },
    reportingPeriods: {
      type: Array as PropType<Array<string>>,
      required: true,
    },
  },
  emits: ['closeDownloadModal', 'downloadDataset'],
  data() {
    return {
      exportFileTypes: [ExportFileTypes.CsvFile, ExportFileTypes.ExcelFile, ExportFileTypes.JsonFile],
      selectedReportingPeriod: '',
      selectedFileFormat: '',
      isModalVisible: false,
      showReportingPeriodError: false,
      showFileFormatError: false,
    };
  },

  computed: {
    fileTypeSelectionOptions() {
      return Object.values(this.exportFileTypes).map((fileType) => ({
        value: fileType.identifier,
        label: `${fileType.description} (.${fileType.fileExtension})`,
      }));
    },
  },

  watch: {
    isDownloadModalOpen(newValue: boolean): void {
      this.isModalVisible = newValue;
    },
    selectedReportingPeriod() {
      this.showReportingPeriodError = false;
    },
    selectedFileFormat() {
      this.showFileFormatError = false;
    },
  },

  methods: {
    /**
     * Handle the clickEvent of the Download Button
     */
    onDownloadButtonClick() {
      this.checkIfShowErrors();

      if (this.showReportingPeriodError || this.showFileFormatError) {
        return;
      }

      this.$emit('downloadDataset', this.selectedReportingPeriod, this.selectedFileFormat);
      this.closeDialog();
    },

    /**
     * Validates the form's fields and displays errors if necessary
     */
    checkIfShowErrors() {
      this.showReportingPeriodError = this.selectedReportingPeriod.length === 0;
      this.showFileFormatError = this.selectedFileFormat.length === 0;
    },

    /**
     * Resets selections and error messages and closes the download modal
     */
    closeDialog() {
      this.resetProps();
      this.isModalVisible = false;
      this.$emit('closeDownloadModal');
    },

    /**
     * Resets the props, e.g. the selections and error messages.
     */
    resetProps() {
      this.selectedReportingPeriod = '';
      this.selectedFileFormat = '';
      this.showReportingPeriodError = false;
      this.showFileFormatError = false;
    },
  },
});
</script>
