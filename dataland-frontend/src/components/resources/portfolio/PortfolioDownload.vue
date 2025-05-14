<template>
  <div class="portfolio-download-content d-flex flex-column align-items-center">
    <FormKit type="form" style="flex-grow: 1" :actions="false">
      <label for="frameworkSelector">
        <span>Framework</span>
      </label>
      <FormKit
        :options="availableFrameworks"
        v-model="selectedFramework"
        data-test="frameworkSelector"
        type="select"
        name="frameworkSelector"
        placeholder="Select framework"
        @input="onFrameworkChange"
      />
      <p v-show="showFrameworksError" class="text-danger" data-test="frameworkError">Please select Framework.</p>
      <label for="reportingYearSelector">
        <span>Reporting Year</span>
      </label>
      <div class="flex flex-wrap gap-2 py-2">
        <ToggleChipFormInputs
          :key="selectedFramework || 'no-framework'"
          :name="'listOfReportingPeriods'"
          :options="allReportingPeriodsOptions"
          :availableOptions="availableReportingPeriods"
          data-test="listOfReportingPeriods"
          class="toggle-chip-group"
          @changed="resetErrors"
        />
      </div>
      <p v-if="showReportingPeriodsError" class="text-danger mt-2" data-test="reportingPeriodsError">
        Please select at least one Reporting Period.
      </p>
      <label for="fileTypeSelector">
        <span>File Type</span>
      </label>
      <FormKit
        v-model="selectedFileType"
        data-test="fileTypeSelector"
        type="select"
        name="fileTypeSelector"
        placeholder="Select file type"
        :options="fileTypeSelectionOptions"
      />
      <p v-show="showFileTypeError" class="text-danger" data-test="fileTypeError">Please select a File Type.</p>
      <div class="flex align-content-start align-items-center">
        <InputSwitch v-model="keepValuesOnly" class="form-field vertical-middle" data-test="valuesOnlySwitch" />
        <span data-test="portfolioExportValuesOnlyToggleCaption" class="ml-2"> Values only </span>
      </div>
      <span class="gray-text font-italic text-xs ml-0 mb-3">
        Download only data values. Turn off to include additional details, e.g. comment, data source, ...
      </span>
    </FormKit>
    <Message v-if="portfolioErrors" severity="error" class="my-1 text-xs" :life="3000">
      {{ portfolioErrors }}
    </Message>
    <template v-if="!isDownloading">
      <PrimeButton
        data-test="downloadButton"
        class="primary-button my-2"
        label="Download Portfolio"
        icon="pi pi-download"
        title="Download the selected frameworks and reporting periods for current portfolio"
        @click="handlePortfolioDownload"
      />
    </template>
    <template v-else>
      <div class="my-4" data-test="downloadSpinner">
        <DownloadProgressSpinner :percentCompleted="downloadProgress" :white-spinner="true" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import ToggleChipFormInputs, { type ToggleChipInputType } from '@/components/general/ToggleChipFormInputs.vue';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import {
  createNewPercentCompletedRef,
  downloadIsInProgress,
} from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { forceFileDownload } from '@/utils/FileDownloadUtils.ts';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { type DropdownOption } from '@/utils/PremadeDropdownDatasets.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type CompanyIdAndName, ExportFileType } from '@clients/backend';
import { type EnrichedPortfolio, type EnrichedPortfolioEntry } from '@clients/userservice';
import { type AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import InputSwitch from 'primevue/inputswitch';
import Message from 'primevue/message';
import { inject, onMounted, type Ref, ref } from 'vue';

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const portfolioName = ref<string>('');
const portfolioEntries = ref<EnrichedPortfolioEntry[]>([]);
const selectedFramework = ref<string | undefined>(undefined);
const selectedFileType = ref(undefined);

const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const keepValuesOnly = ref(true);
const showFileTypeError = ref(false);
const showReportingPeriodsError = ref(false);
const showFrameworksError = ref(false);
const portfolioErrors = ref('');

const availableFrameworks: DropdownOption[] = MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER.map((framework) => ({
  value: framework,
  label: humanizeStringOrNumber(framework),
}));
const ALL_REPORTING_PERIODS = [2025, 2024, 2023, 2022, 2021, 2020];
const ALL_EXPORT_FILE_TYPES = [ExportFileType.Csv, ExportFileType.Excel];

const allReportingPeriodsOptions = ref<ToggleChipInputType[]>();
const availableReportingPeriods = ref<Array<ToggleChipInputType>>([]);

const fileTypeSelectionOptions: DropdownOption[] = ALL_EXPORT_FILE_TYPES.map((type) => {
  const information = ExportFileTypeInformation[type];
  return {
    value: type,
    label: `${information.description} (.${information.fileExtension})`,
  };
});

const downloadProgress = ref<number | undefined>(undefined);
const isDownloading = ref(false);
const percentCompleted = createNewPercentCompletedRef();

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioName.value = portfolio.portfolioName;
    portfolioEntries.value = portfolio.entries;
    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.entries);
  }
  allReportingPeriodsOptions.value = ALL_REPORTING_PERIODS.map((period) => ({
    name: period.toString(),
    value: false,
  }));
});

/**
 * When the framework changes, update the available reporting periods based on the selected framework
 */
function onFrameworkChange(newFramework: string | undefined): void {
  resetErrors();
  if (!newFramework) {
    availableReportingPeriods.value = [];
    return;
  }
  selectedFramework.value = newFramework;
  allReportingPeriodsOptions.value?.forEach((option) => {
    option.value = false;
  });
  updateAvailableReportingPeriods(newFramework);
}

/**
 * Updates the available reporting periods based on the selected framework
 * by extracting data from portfolio entries
 * @param framework The framework to get available reporting periods for
 */
function updateAvailableReportingPeriods(framework: string): void {
  if (!portfolioEntries.value || portfolioEntries.value.length === 0) {
    availableReportingPeriods.value = [];
    return;
  }

  const availablePeriods = new Set<string>();

  portfolioEntries.value.forEach((entry) => {
    if (entry.availableReportingPeriods && entry.availableReportingPeriods[framework]) {
      const periods = entry.availableReportingPeriods[framework].split(',').map((p) => p.trim());
      periods.forEach((period: string) => {
        if (period && period !== 'No data available') {
          availablePeriods.add(period);
        }
      });
    }
  });

  availableReportingPeriods.value = Array.from(availablePeriods)
    .sort((a, b) => parseInt(b) - parseInt(a))
    .map((period) => ({
      name: period,
      value: false,
    }));
}

/**
 * Reset errors when either framework, reporting period or file type changes
 */
function resetErrors(): void {
  portfolioErrors.value = '';
  showReportingPeriodsError.value = false;
  showFileTypeError.value = false;
  showFrameworksError.value = false;
}

/**
 * Retrieve the array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Extracts currently selected reporting periods
 */
function getSelectedReportingPeriods(): string[] {
  if (!allReportingPeriodsOptions.value) return [];
  return allReportingPeriodsOptions.value.filter((period) => period.value).map((period) => period.name);
}

/**
 * Handle the clickEvent of the Download Button
 */
async function handlePortfolioDownload(): Promise<void> {
  portfolioErrors.value = '';
  checkIfShowErrors();
  if (showReportingPeriodsError.value || showFileTypeError.value || showFrameworksError.value) {
    return;
  }

  isDownloading.value = true;
  downloadProgress.value = 0;

  if (downloadIsInProgress(percentCompleted.value)) return;
  await downloadPortfolio();
}

/**
 * Extracts company IDs from the selected portfolio
 */
function getCompanyIds(): string[] {
  return portfolioCompanies.value.map((company) => company.companyId);
}

/**
 * Checks for visible errors
 */
function checkIfShowErrors(): void {
  showReportingPeriodsError.value = getSelectedReportingPeriods().length === 0;
  showFileTypeError.value = !selectedFileType.value;
  showFrameworksError.value = !selectedFramework.value;
}

/**
 * Handles download of portfolio
 */
async function downloadPortfolio(): Promise<void> {
  try {
    if (!selectedFramework.value || !selectedFileType.value) return;

    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const frameworkDataApi = getFrameworkDataApiForIdentifier(
      selectedFramework.value,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const dataResponse = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
      getSelectedReportingPeriods(),
      getCompanyIds(),
      selectedFileType.value,
      keepValuesOnly.value
    );

    if (dataResponse.status === 204) {
      portfolioErrors.value = 'No data available.';
      isDownloading.value = false;
      return;
    }

    const dataContent =
      selectedFileType.value === ExportFileType.Csv ? JSON.stringify(dataResponse.data) : dataResponse.data;

    forceFileDownload(dataContent, `Portfolio-${portfolioName.value}-${selectedFramework.value}.csv`);
  } catch (error) {
    console.error(error);
    portfolioErrors.value = `${(error as AxiosError).message}`;
  } finally {
    isDownloading.value = false;
    downloadProgress.value = undefined;
  }
}
</script>

<style scoped lang="scss">
@use '@/assets/scss/variables.scss';

.portfolio-download-content {
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

.formkit-help {
  color: variables.$gray;
  font-size: variables.$fs-xs;
  font-style: italic;
}
</style>
