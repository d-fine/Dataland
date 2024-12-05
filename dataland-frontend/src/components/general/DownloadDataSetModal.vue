<template>
  <PrimeDialog
    v-if="isModalVisible"
    v-model:visible="isModalVisible"
    style="text-align: center; width: 20%"
    :show-header="true"
    header="Download dataset"
    :closable="true"
    :dismissable-mask="true"
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
      <label for="Download">
        <b style="margin-bottom: 8px; font-weight: normal">File format</b>
      </label>
      <FormKit
        v-model="selectedFormat"
        type="select"
        name="formatSelector"
        data-test="formatSelector"
        :options="['json', 'csv']"
        placeholder="Select a file format"
      />
    </FormKit>
    <div>
      <PrimeButton
        data-test="downloadDataButton"
        @click="downloadData()"
        style="width: 100%; justify-content: center"
        :disabled="selectedFormat === '' || selectedReportingPeriod === ''"
      >
        <span class="d-letters" style="text-align: center" data-test="downloadButton"> DOWNLOAD </span>
      </PrimeButton>
    </div>
  </PrimeDialog>
</template>

<script lang="ts">
import { defineComponent, type PropType, computed, ref, watch } from 'vue';
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import type { DataMetaInformation, DataTypeEnum } from '@clients/backend';

export default defineComponent({
  components: { PrimeDialog, PrimeButton },
  name: 'DownloadDataSetModal',
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
    const selectedFormat = ref('');

    watch(
      () => props.isDownloadModalOpen,
      (newValue) => {
        isModalVisible.value = newValue;
      }
    );

    const downloadData = (): void => {
      if (selectedReportingPeriod.value && selectedFormat.value) {
        props.handleDownload(selectedReportingPeriod, selectedFormat);
      }
      closeModal();
      resetSelection();
    };

    const closeModal = (): void => {
      isModalVisible.value = false;
      emit('update:isDownloadModalOpen', false);
    };
    const resetSelection = (): void => {
      selectedReportingPeriod.value = '';
      selectedFormat.value = '';
    };

    return {
      isModalVisible,
      reportingPeriodOptions,
      selectedReportingPeriod,
      selectedFormat,
      downloadData,
    };
  },
});
</script>

<style></style>
