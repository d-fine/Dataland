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
        @change="onFrameworkChange"
      />
      <Message v-if="showFrameworksError" severity="error" variant="simple" size="small" data-test="frameworkError">
        Please select a framework.
      </Message>
    </div>
    <div>
      <p class="header-styling">Reporting Period</p>
      <div class="toggle-switch-wrapper">
        <ToggleSwitch v-model="latestOnly" data-test="latestReportingPeriodSwitch" @change="onLatestOnlyChange" />
        <p>Latest reporting period only</p>
      </div>
      <MultiSelect
        v-model="selectedReportingPeriods"
        :options="availableReportingPeriodOptions"
        option-label="label"
        option-value="value"
        option-disabled="disabled"
        placeholder="Select reporting periods"
        data-test="reportingPeriodSelector"
        class="component-styling"
        :disabled="latestOnly"
        :highlightOnSelect="false"
        :showToggleAll="false"
        fluid
        @change="showReportingPeriodError = false"
      />
      <div v-if="latestOnly" class="dataland-info-text small">
        For each company in the portfolio, the latest available data is exported.
      </div>
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
      <div class="dataland-info-text small">
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
    <div class="dataland-info-text small">
      Use short aliases, e. g. REV_ELIGIBLE_ABS in export. Only applicable for CSV and Excel file types.
    </div>
    <Message v-if="dialogRef?.data?.downloadErrors" severity="error" class="mt-2">
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
import ToggleSwitch from 'primevue/toggleswitch';
import MultiSelect from 'primevue/multiselect';
import type { DataTypeEnum } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER, DOWNLOADABLE_DATA_REPORTING_PERIODS } from '@/utils/Constants.ts';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import Message from 'primevue/message';
import PrimeSelect, { type SelectChangeEvent } from 'primevue/select';

const emit = defineEmits<{
  (emit: 'closeDownloadModal'): void;
  (
    emit: 'downloadDataset',
    reportingPeriod: string[],
    fileType: string,
    selectedFramework: string,
    keepValuesOnly: boolean,
    includeAlias: boolean,
    latestOnly: boolean
  ): void;
}>();

const downloadProgress = ref<number | undefined>(undefined);
const selectedFileType = ref<string>('');
const showReportingPeriodError = ref<boolean>(false);
const showFrameworksError = ref<boolean>(false);
const showFileTypeError = ref<boolean>(false);
const selectedReportingPeriods = ref<string[]>([]);
const keepValuesOnly = ref(true);
const includeAlias = ref(true);
const latestOnly = ref(true);
const selectedFramework = ref<DataTypeEnum | undefined>(undefined);
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const reportingPeriodsPerFramework = ref<Map<string, string[]>>(new Map());
const fileTypeSelectionOptions = computed(() => {
  return Object.entries(ExportFileTypeInformation).map(([type, info]) => ({
    value: type,
    label: `${info.description} (.${info.fileExtension})`,
  }));
});

const availableFrameworks = computed(() => {
  const frameworks = new Set(reportingPeriodsPerFramework.value.keys());

  return ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) => frameworks.has(framework)).map((framework) => ({
    value: framework,
    label: humanizeStringOrNumber(framework),
  }));
});

const availableReportingPeriodOptions = computed(() => {
  const reportingPeriods = selectedFramework.value
    ? (reportingPeriodsPerFramework.value.get(selectedFramework.value) ?? [])
    : [];

  return DOWNLOADABLE_DATA_REPORTING_PERIODS.map((period) => ({
    value: period,
    label: period,
    disabled: !reportingPeriods.includes(period),
  }));
});

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.reportingPeriodsPerFramework) {
    reportingPeriodsPerFramework.value = new Map(data.reportingPeriodsPerFramework);
  }

  if (!selectedFramework.value) {
    selectedFramework.value = availableFrameworks.value[0]?.value as DataTypeEnum | undefined;
  }
});

/**
 * Handles changing framework selections
 * @param event - SelectChangeEvent from PrimeSelect
 */
function onFrameworkChange(event: SelectChangeEvent): void {
  resetErrors();
  selectedFramework.value = event.value as DataTypeEnum;
  selectedReportingPeriods.value = [];
}

/**
 * Handles toggling the latest-only switch
 */
function onLatestOnlyChange(): void {
  resetErrors();
  if (latestOnly.value) {
    selectedReportingPeriods.value = [];
  }
}

/**
 * Reset errors when either framework, reporting period or file type changes
 */
function resetErrors(): void {
  showReportingPeriodError.value = false;
  showFileTypeError.value = false;
  showFrameworksError.value = false;
}

/**
 * Handles the clickEvent of the Download Button
 */
function onDownloadButtonClick(): void {
  checkIfShowErrors();
  downloadProgress.value = 0;

  if (showReportingPeriodError.value || showFileTypeError.value) {
    return;
  }

  emit(
    'downloadDataset',
    selectedReportingPeriods.value,
    selectedFileType.value,
    selectedFramework.value ?? '',
    keepValuesOnly.value,
    includeAlias.value,
    latestOnly.value
  );
}

/**
 * Validates the form's fields and displays errors if necessary
 */
function checkIfShowErrors(): void {
  showReportingPeriodError.value = !latestOnly.value && selectedReportingPeriods.value.length === 0;
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
</style>
