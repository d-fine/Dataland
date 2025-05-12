<template>
  <div class="portfolio-download-content d-flex flex-column align-items-center">
    <FormKit type="form" style="flex-grow: 1" :actions="false">
      <label for="frameworkSelector">
        <span>Framework</span>
      </label>
      <FormKit
        v-model="selectedFramework"
        data-test="frameworkSelector"
        type="select"
        name="frameworkSelector"
        placeholder="Select framework"
        :options="availableFrameworks"
        @input="resetErrors"
      />
      <p v-show="showFrameworksError" class="text-danger" data-test="frameworkError">Please select Framework.</p>
      <label for="reportingYearSelector">
        <span>Reporting Year</span>
      </label>
      <div class="flex flex-wrap gap-2 py-2">
        <ToggleChipFormInputs
          data-test="listOfReportingPeriods"
          class="toggle-chip-group"
          :options="dynamicReportingPeriods"
          :name="'listOfReportingPeriods'"
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
      <FormKit
        v-model="includeMetaData"
        data-test="includeMetaData"
        type="checkbox"
        name="includeMetaData"
        label="Include Meta Data"
        help="Download values with additional Meta data."
        :outer-class="{
          'yes-no-radio': true,
        }"
        :inner-class="{
          'formkit-inner': false,
        }"
        :input-class="{
          'formkit-input': false,
          'p-radiobutton': true,
        }"
      />
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
        @click="onDownloadButtonClick"
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
import { inject, onMounted, type Ref, ref } from 'vue';
import PrimeButton from 'primevue/button';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import { type EnrichedPortfolio, type EnrichedPortfolioEntry } from '@clients/userservice';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { type CompanyIdAndName, ExportFileType } from '@clients/backend';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import type Keycloak from 'keycloak-js';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import Message from 'primevue/message';
import DownloadProgressSpinner from '@/components/resources/frameworkDataSearch/DownloadProgressSpinner.vue';
import { forceFileDownload } from '@/utils/FileDownloadUtils.ts';

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const portfolioName = ref<string>('');
const portfolioEntries = ref<EnrichedPortfolioEntry[]>([]);
const selectedFramework = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const includeMetaData = ref(false);
const showFileTypeError = ref(false);
const showReportingPeriodsError = ref(false);
const showFrameworksError = ref(false);
const portfolioErrors = ref('');
const selectedFileType = ref<'CSV' | 'EXCEL' | undefined>(undefined);
const availableFrameworks = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'eutaxonomy-financials', label: 'EU Taxonomy Financials' },
  { value: 'eutaxonomy-non-financials', label: 'EU Taxonomy Non Financials' },
  { value: 'nuclear-and-gas', label: 'Nuclear and Gas' },
];
const dynamicReportingPeriods = ref(
  [2025, 2024, 2023, 2022, 2021, 2020].map((year) => ({
    name: year.toString(),
    value: false,
  }))
);
const fileTypeSelectionOptions = [
  { label: 'Comma-separated Values (.csv)', value: ExportFileType.Csv },
  { label: 'Excel-compatible CSV File (.csv)', value: ExportFileType.Excel },
];
const downloadProgress = ref<number | undefined>(undefined);
const isDownloading = ref(false);
const props = defineProps<{
  portfolioId: string;
}>();

const portfolioId = ref<string | undefined>(props.portfolioId);

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioId.value = portfolio.portfolioId;
    portfolioName.value = portfolio.portfolioName;
    portfolioEntries.value = portfolio.entries;
    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.entries);
  }
});

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
 * Retrieve array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Extracts currently selected reporting periods
 */
function getSelectedReportingPeriods(): string[] {
  return dynamicReportingPeriods.value.filter((period) => period.value).map((period) => period.name);
}

/**
 /**
 * Handle the clickEvent of the Download Button
 */
async function onDownloadButtonClick(): Promise<void> {
  portfolioErrors.value = '';
  checkIfShowErrors();
  if (showReportingPeriodsError.value || showFileTypeError.value || showFrameworksError.value) {
    return;
  }

  isDownloading.value = true;
  downloadProgress.value = 0;

  try {
    await downloadPortfolio();
  } catch (error) {
    console.error('Download error 1:', error);
    if (error instanceof Error && error.message.includes('CSV data is empty')) {
      portfolioErrors.value = 'No data available.';
    } else {
      portfolioErrors.value = 'Network error. Please try again';
    }
    isDownloading.value = false;
    downloadProgress.value = undefined;
  }
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
      includeMetaData.value
    );

    const dataContent =
      selectedFileType.value === ExportFileType.Csv ? JSON.stringify(dataResponse.data) : dataResponse.data;

    forceFileDownload(dataContent, `Portfolio-${portfolioName.value}-${selectedFramework.value}.csv`);
  } catch (error) {
    console.error(error);
    portfolioErrors.value = 'Download failed due to an unexpected error.';
    isDownloading.value = false;
    downloadProgress.value = undefined;
  }
}
</script>

<style lang="scss">
@import '@/assets/scss/variables.scss';

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
  color: $gray;
  font-size: $fs-xs;
  font-style: italic;
}

.p-dialog-title {
  max-width: 15em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
