<template>
  <PrimeDialog
    v-model:visible="isModalVisible"
    style="text-align: center; width: 20%"
    :show-header="true"
    header="Download dataset"
    :closable="true"
    :dismissable-mask="true"
    @hide="closeModal"
  >
    <FormKit type="form" :actions="false" class="formkit-wrapper">
      <label for="Download">
        <b style="margin-bottom: 8px; font-weight: normal">Reporting year</b>
      </label>
      <FormKit
        v-model="selectedReportingPeriod"
        type="select"
        name="reportingYearSelector"
        data-test="reportingYearSelector"
        :options="reportingPeriodOptions"
        placeholder="Select a reporting year"
      />
      <p v-show="showReportingPeriodError" class="text-danger text-xs" data-test="noReportingYearError">
        Please select a reporting period.
      </p>
      <label for="Download">
        <b style="margin-bottom: 8px; margin-top: 5px; font-weight: normal">File format</b>
      </label>
      <FormKit
        v-model="selectedFileFormat"
        type="select"
        name="formatSelector"
        data-test="formatSelector"
        :options="['json', 'csv']"
        placeholder="Select a file format"
      />
      <p v-show="showFileFormatError" class="text-danger text-xs" data-test="noFileFormatError">
        Please select a file format.
      </p>
    </FormKit>

    <div>
      <PrimeButton data-test="downloadDataButton" @click="downloadFile()" style="width: 100%; justify-content: center">
        <span class="d-letters" style="text-align: center" data-test="downloadButton"> DOWNLOAD </span>
      </PrimeButton>
    </div>
  </PrimeDialog>
</template>

<script lang="ts">
import { defineComponent, ref, watch, type PropType, computed } from 'vue';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import { type DataMetaInformation, type DataTypeEnum } from '@clients/backend';

export default defineComponent({
  components: { PrimeDialog, PrimeButton },
  name: 'DownloadDatasetModal',
  props: {
    isDownloadModalOpen: {
      type: Boolean,
      required: true,
    },
    handleDownload: {
      type: Function,
      required: true,
    },
    dataType: {
      type: String as PropType<DataTypeEnum>,
      required: true,
    },
    mapOfReportingPeriodToActiveDataset: {
      type: Map as PropType<Map<string, DataMetaInformation>>,
    },
  },
  data() {
    return {
      showReportingPeriodError: false,
      showFileFormatError: false,
    };
  },
  setup(props, { emit }) {
    const getReportingPeriods = (mapping: Map<String, DataMetaInformation>): string[] => {
      const reportingPeriods = new Set<string>();
      mapping.forEach((dataMetaInfo) => {
        if (dataMetaInfo.dataType === props.dataType) {
          if (dataMetaInfo.currentlyActive) {
            reportingPeriods.add(dataMetaInfo.reportingPeriod);
          }
        }
      });
      return Array.from(reportingPeriods);
    };
    const reportingPeriodOptions = computed(() => {
      if (!props.mapOfReportingPeriodToActiveDataset) {
        return [];
      }
      return getReportingPeriods(props.mapOfReportingPeriodToActiveDataset).sort();
    });

    const isModalVisible = ref(props.isDownloadModalOpen);
    const selectedReportingPeriod = ref('');
    const selectedFileFormat = ref('');

    watch(
      () => props.isDownloadModalOpen,
      (newValue) => {
        isModalVisible.value = newValue;
      }
    );

    const onHide = (): void => {
      closeModal();
      resetSelection();
    };

    const closeModal = (): void => {
      isModalVisible.value = false;
      emit('update:isDownloadModalOpen', false);
    };

    const resetSelection = (): void => {
      selectedReportingPeriod.value = '';
      selectedFileFormat.value = '';
    };

    return {
      isModalVisible,
      selectedReportingPeriod,
      selectedFileFormat,
      reportingPeriodOptions,
      onHide,
    };
  },
  methods: {
    /**
     * downloads the requested dataset as a file with the selected file format
     */
    downloadFile() {
      if (this.selectedReportingPeriod && this.selectedFileFormat) {
        this.handleDownload(this.selectedReportingPeriod, this.selectedFileFormat);

        this.closeModal();
        return;
      }
      this.showReportingPeriodError = true;
      this.showFileFormatError = true;
    },
    /**
     * closes the download modal and removed error messages (if format or reporting period were not selected)
     */
    closeModal() {
      this.onHide();
      this.showReportingPeriodError = false;
      this.showFileFormatError = false;
    },
  },
});
</script>
