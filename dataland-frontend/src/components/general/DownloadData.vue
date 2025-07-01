<template data-test="downloadModal">
  <div class="download-content d-flex flex-column align-items-center">
    <FormKit type="form" class="formkit-wrapper" :actions="false">
      <label for="fileTypeSelector">
        <b style="margin-bottom: 8px; margin-top: 5px; font-weight: normal">Framework</b>
      </label>
      <FormKit
        :options="availableFrameworks"
        v-model="selectedFramework"
        data-test="frameworkSelector"
        type="select"
        name="frameworkSelector"
        @input="onFrameworkChange"
      />
      <p v-show="showFrameworksError" class="text-danger" data-test="frameworkError">Please select Framework.</p>
      <label for="reportingYearSelector">
        <b style="margin-bottom: 8px; font-weight: normal">Reporting period</b>
      </label>
      <div class="flex flex-wrap gap-2 py-2">
        <ToggleChipFormInputs
          :key="selectedFramework || 'no-framework'"
          name="listOfReportingPeriods"
          :selectedOptions="selectableReportingPeriodOptions"
          :availableOptions="allReportingPeriodOptions.filter((option) => option.value)"
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
        @change="showFileTypeError = false"
      />
      <p v-show="showFileTypeError" class="text-danger" data-test="fileTypeError">Please select a file type.</p>
      <div class="flex align-content-start align-items-center">
        <InputSwitch
          v-model="keepValuesOnly"
          class="form-field vertical-middle"
          data-test="valuesOnlySwitch"
          @change="!keepValuesOnly ? (includeAlias = false) : includeAlias"
        />
        <span data-test="portfolioExportValuesOnlyToggleCaption" class="ml-2"  style="margin-top: 1rem; margin-bottom: 1.5rem;"> Values only </span>
      </div>
      <span class="gray-text font-italic text-xs ml-0 mb-3">
        Download only data values. Turn off to include additional details, e.g. comment, data source, ...
      </span>
      <div class="flex align-content-start align-items-center">
        <InputSwitch
          v-model="includeAlias"
          :disabled="!keepValuesOnly"
          class="form-field vertical-middle"
          data-test="includeAliasSwitch"
        />
        <span data-test="portfolioExportIncludeAliasToggleCaption" class="ml-2" style="margin-top: 1rem; margin-bottom: 1.5rem;"> Shorten field names </span>
      </div>
      <span class="gray-text font-italic text-xs ml-0 mb-3">
        Use short aliases, e. g. REV_ELIGIBLE_ABS in export. (only applicable if values only is selected)
      </span>
    </FormKit>
    <div>
      <PrimeButton
        data-test="downloadDataButtonInModal"
        @click="onDownloadButtonClick()"
        label="DOWNLOAD"
        class="primary-button my-2"
        icon="pi pi-download"
        :loading="dialogRef?.data?.isDownloading"
        style="width: 100%"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, onMounted, type Ref, ref } from 'vue';
import PrimeButton from 'primevue/button';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import ToggleChipFormInputs, { type ToggleChipInputType } from '@/components/general/ToggleChipFormInputs.vue';
import InputSwitch from 'primevue/inputswitch';
import type { DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

const emit = defineEmits<{
  (emit: 'closeDownloadModal'): void;
  (
    emit: 'downloadDataset',
    reportingPeriod: string[],
    fileType: string,
    selectedFramework: string,
    keepValuesOnly: boolean,
    includeAlias: boolean
  ): void;
}>();

const downloadProgress = ref<number | undefined>(undefined);
const portfolioErrors = ref('');
const selectedFileType = ref<string>('');
const showReportingPeriodError = ref<boolean>(false);
const showFrameworksError = ref<boolean>(false);
const showFileTypeError = ref<boolean>(false);
const allReportingPeriodOptions = ref<ToggleChipInputType[]>([]);
const selectableReportingPeriodOptions = ref<ToggleChipInputType[]>([]);
const keepValuesOnly = ref(true);
const includeAlias = ref(true);
const selectedFramework = ref<DataTypeEnum | undefined>(undefined);
const ALL_REPORTING_PERIODS = [2025, 2024, 2023, 2022, 2021, 2020];
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const reportingPeriodsPerFramework = ref<Map<string, string[]>>(new Map());
const fileTypeSelectionOptions = computed(() => {
  return Object.entries(ExportFileTypeInformation).map(([type, info]) => ({
    value: type,
    label: `${info.description} (.${info.fileExtension})`,
  }));
});

const availableFrameworks = computed(() => {
  const frameworks = Array.from(reportingPeriodsPerFramework.value.keys());

  return ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) => frameworks.includes(framework)).map((framework) => ({
    value: framework,
    label: humanizeStringOrNumber(framework),
  }));
});

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.reportingPeriodsPerFramework) {
    reportingPeriodsPerFramework.value = new Map(data.reportingPeriodsPerFramework);
  }

  console.log(data?.isDownloading);

  selectableReportingPeriodOptions.value = ALL_REPORTING_PERIODS.map((period) => ({
    name: period.toString(),
    value: false,
  }));
  if (selectedFramework.value) {
    onFrameworkChange(selectedFramework.value);
  }
  onModalOpen();
});

/**
 * Reset errors when either framework, reporting period or file type changes
 */
function resetErrors(): void {
  portfolioErrors.value = '';
  showReportingPeriodError.value = false;
  showFileTypeError.value = false;
  showFrameworksError.value = false;
}

/**
 * Handles changing framework selections
 * @param framework selected framework by user
 */
function onFrameworkChange(framework: string | undefined): void {
  resetErrors();

  const reportingPeriods = framework ? (reportingPeriodsPerFramework.value.get(framework) ?? []) : [];

  selectedFramework.value = framework as DataTypeEnum;

  allReportingPeriodOptions.value = ALL_REPORTING_PERIODS.map((period) => ({
    name: period.toString(),
    value: reportingPeriods.includes(period.toString()),
  }));

  selectableReportingPeriodOptions.value = ALL_REPORTING_PERIODS.map((period) => ({
    name: period.toString(),
    value: false,
  }));
}

/**
 * Sets predefined framework for default
 */
function onModalOpen(): void {
  if (!selectedFramework.value) {
    selectedFramework.value = availableFrameworks.value[0]?.value as DataTypeEnum | undefined;
  }
  onFrameworkChange(selectedFramework.value);
}

/**
 * Handles the clickEvent of the Download Button
 */
function onDownloadButtonClick(): void {
  const selectedReportingPeriods = getSelectedReportingPeriods();

  checkIfShowErrors();
  downloadProgress.value = 0;

  if (showReportingPeriodError.value || showFileTypeError.value) {
    return;
  }

  emit(
    'downloadDataset',
    selectedReportingPeriods,
    selectedFileType.value,
    selectedFramework.value ?? '',
    keepValuesOnly.value,
    includeAlias.value
  );
}

/**
 * Extracts currently selected reporting periods
 */
function getSelectedReportingPeriods(): string[] {
  if (!selectableReportingPeriodOptions.value) return [];
  return selectableReportingPeriodOptions.value.filter((period) => period.value).map((period) => period.name);
}

/**
 * Validates the form's fields and displays errors if necessary
 */
function checkIfShowErrors(): void {
  showReportingPeriodError.value = getSelectedReportingPeriods().length === 0;
  showFileTypeError.value = selectedFileType.value.length === 0;
}
</script>

<style scoped lang="scss">
@use '@/assets/scss/variables.scss';

.download-content {
  width: 20em;
  height: 100%;
  border-radius: 0.25rem;
  background-color: white;
  padding: 0.5rem 1.5rem;
  display: flex;
  flex-direction: column;
}
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
