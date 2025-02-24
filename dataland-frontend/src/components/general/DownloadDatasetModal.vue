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
      <label for="fileTypeSelector">
        <b style="margin-bottom: 8px; margin-top: 5px; font-weight: normal">File Type</b>
      </label>
      <FormKit
        v-model="selectedFileType"
        type="select"
        name="fileTypeSelector"
        data-test="fileTypeSelector"
        :options="fileTypeSelectionOptions"
        placeholder="Select a file type"
      />
      <p v-show="showFileTypeError" class="text-danger text-xs" data-test="fileTypeError">Please select a file type.</p>
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
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';

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
      selectedReportingPeriod: '',
      selectedFileType: '',
      isModalVisible: false,
      showReportingPeriodError: false,
      showFileTypeError: false,
    };
  },
  computed: {
    fileTypeSelectionOptions() {
      return Object.entries(ExportFileTypeInformation).map(([type, information]) => ({
        value: type.toString(),
        label: `${information.description} (.${information.fileExtension})`,
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
    selectedFileType() {
      this.showFileTypeError = false;
    },
  },

  methods: {
    /**
     * Handle the clickEvent of the Download Button
     */
    onDownloadButtonClick() {
      this.checkIfShowErrors();

      if (this.showReportingPeriodError || this.showFileTypeError) {
        return;
      }

      this.$emit('downloadDataset', this.selectedReportingPeriod, this.selectedFileType);
      this.closeDialog();
    },

    /**
     * Validates the form's fields and displays errors if necessary
     */
    checkIfShowErrors() {
      this.showReportingPeriodError = this.selectedReportingPeriod.length === 0;
      this.showFileTypeError = this.selectedFileType.length === 0;
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
      this.selectedFileType = '';
      this.showReportingPeriodError = false;
      this.showFileTypeError = false;
    },
  },
});
</script>
