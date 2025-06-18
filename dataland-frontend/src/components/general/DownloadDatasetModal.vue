<template>
  <PrimeDialog
    v-model:visible="isModalVisible"
    style="text-align: center; width: 20em"
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
      <div class="flex flex-wrap gap-2 py-2">
        <ToggleChipFormInputs
          :name="'listOfReportingPeriods'"
          :options="allReportingPeriodOptions"
          :availableOptions="mappedReportingPeriods"
          data-test="listOfReportingPeriods"
          class="toggle-chip-group"
        />
      </div>
      <p v-show="showReportingPeriodError" class="text-danger" data-test="reportingYearError">
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
      <p v-show="showFileTypeError" class="text-danger" data-test="fileTypeError">Please select a file type.</p>
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

<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import ToggleChipFormInputs, { type ToggleChipInputType } from '@/components/general/ToggleChipFormInputs.vue';

const props = defineProps<{
  isDownloadModalOpen?: boolean;
  reportingPeriods: Array<ToggleChipInputType>;
}>();

const emit = defineEmits<{
  (e: 'closeDownloadModal'): void;
  (e: 'downloadDataset', reportingPeriod: string[], fileType: string): void;
}>();

const isDownloadModalOpen = toRef(props, 'isDownloadModalOpen');
const selectedFileType = ref<string>('');
const isModalVisible = ref<boolean>(false);
const showReportingPeriodError = ref<boolean>(false);
const showFileTypeError = ref<boolean>(false);
const allReportingPeriodOptions = ref<ToggleChipInputType[] | undefined>();
const ALL_REPORTING_PERIODS = [2025, 2024, 2023, 2022, 2021, 2020];

allReportingPeriodOptions.value = ALL_REPORTING_PERIODS.map((period) => ({
  name: period.toString(),
  value: false,
}));

const mappedReportingPeriods = computed(() => {
  return props.reportingPeriods.map((p) => ({
    name: p.name ?? '',
    value: p.value,
  }));
});

const fileTypeSelectionOptions = computed(() => {
  return Object.entries(ExportFileTypeInformation).map(([type, info]) => ({
    value: type,
    label: `${info.description} (.${info.fileExtension})`,
  }));
});

watch(isDownloadModalOpen, (newVal) => {
  isModalVisible.value = newVal ?? false;
});

watch(selectedFileType, () => {
  showFileTypeError.value = false;
});

/**
 * Handle the clickEvent of the Download Button
 */
function onDownloadButtonClick(): void {
  const selectedReportingPeriods = getSelectedReportingPeriods();

  checkIfShowErrors();

  if (showReportingPeriodError.value || showFileTypeError.value) {
    return;
  }
  emit('downloadDataset', selectedReportingPeriods, selectedFileType.value);
  closeDialog();
}

/**
 * Extracts currently selected reporting periods
 */
function getSelectedReportingPeriods(): string[] {
  if (!allReportingPeriodOptions.value) return [];
  return allReportingPeriodOptions.value.filter((period) => period.value).map((period) => period.name);
}

/**
 * Validates the form's fields and displays errors if necessary
 */
function checkIfShowErrors(): void {
  showReportingPeriodError.value = getSelectedReportingPeriods().length === 0;
  showFileTypeError.value = selectedFileType.value.length === 0;
}

/**
 * Resets selections and error messages and closes the download modal
 */
function closeDialog(): void {
  resetProps();
  isModalVisible.value = false;
  emit('closeDownloadModal');
}

/**
 * Resets the props, e.g. the selections and error messages.
 */
function resetProps(): void {
  selectedFileType.value = '';
  showReportingPeriodError.value = false;
  showFileTypeError.value = false;
}
</script>

<style scoped lang="scss">
.toggle-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;

  .chip {
    font-size: 0.75rem;
    padding: 0.25rem 0.75rem;
    border-radius: 999px;
    white-space: nowrap;
    transition: all 0.2s ease;
    flex: 1 1 auto;
    max-width: 5rem;
    text-align: center;
  }
}
</style>
