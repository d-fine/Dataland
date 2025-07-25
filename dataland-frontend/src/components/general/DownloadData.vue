<template data-test="downloadModal">
  <div class="download-content d-flex flex-column">
    <div>
      <p class="header-styling">Framework</p>
      <PrimeSelect
        class="component-styling"
        :options="availableFrameworks"
        v-model="selectedFramework"
        option-label="label"
        option-value="value"
        label="Framework"
        data-test="frameworkSelector"
        type="select"
        :highlightOnSelect="false"
        @input="onFrameworkChange"
      />
      <Message v-if="showFrameworksError" severity="error" variant="simple" size="small" data-test="frameworkError">
        Please select a framework.
      </Message>
    </div>
    <div>
      <p class="header-styling">Reporting Period</p>
      <ToggleChipFormInputs
        :key="selectedFramework || 'no-framework'"
        name="listOfReportingPeriods"
        :selectedOptions="selectableReportingPeriodOptions"
        :availableOptions="allReportingPeriodOptions.filter((option) => option.value)"
        data-test="listOfReportingPeriods"
        class="toggle-chip-group"
      />
      <Message
        v-if="showReportingPeriodError"
        severity="error"
        variant="simple"
        size="small"
        data-test="reportingYearError"
      >
        Please select a reporting period.
      </Message>
    </div>
    <div>
      <p class="header-styling">File type</p>
      <PrimeSelect
        v-model="selectedFileType"
        type="select"
        data-test="fileTypeSelector"
        :options="fileTypeSelectionOptions"
        option-label="label"
        option-value="value"
        placeholder="Select a file type"
        @change="showFileTypeError = false"
        :highlightOnSelect="false"
        fluid
      />
      <Message v-if="showFileTypeError" severity="error" variant="simple" size="small" data-test="fileTypeError">
        Please select a file type.
      </Message>
    </div>
    <div>
      <div class="toggle-switch-wrapper">
        <ToggleSwitch
          v-model="keepValuesOnly"
          data-test="valuesOnlySwitch"
          @change="!keepValuesOnly ? (includeAlias = false) : includeAlias"
        />
        <p data-test="portfolioExportValuesOnlyToggleCaption">Values only</p>
      </div>
      <div class="dataland-info-text">
        Download only data values. Turn off to include additional details, e.g. comment, data source, etc. Only
        applicable for CSV and Excel file types.
      </div>
    </div>
    <div class="toggle-switch-wrapper">
      <ToggleSwitch
        v-model="includeAlias"
        data-test="includeAliasSwitch"
        @change="includeAlias ? (keepValuesOnly = true) : keepValuesOnly"
      />
      <p data-test="portfolioExportIncludeAliasToggleCaption">Shorten field names</p>
    </div>
    <div class="dataland-info-text">
      Use short aliases, e. g. REV_ELIGIBLE_ABS in export. Only applicable for CSV and Excel file types.
    </div>
    <Message v-if="dialogRef?.data?.downloadErrors" severity="error" :life="3000">
      {{ dialogRef?.data?.downloadErrors }}
    </Message>
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
import ToggleSwitch from 'primevue/toggleswitch';
import type { DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import Message from 'primevue/message';
import PrimeSelect from 'primevue/select';

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

<style scoped>
.toggle-switch-wrapper {
  display: flex;
  align-items: center;
  align-content: flex-start;
  gap: var(--spacing-xs);
}

.header-styling {
  font-weight: var(--font-weight-bold);
}

.component-styling {
  margin-bottom: var(--spacing-xs);
  width: 100%;
}

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
}
</style>
