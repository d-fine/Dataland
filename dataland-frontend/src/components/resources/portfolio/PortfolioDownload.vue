<template>
  <div class="portfolio-download-content">
    <FormKit type="form" class="formkit-wrapper" :actions="false">
      <label for="fileTypeSelector">
        <b style="margin-bottom: 8px; font-weight: normal">Framework</b>
      </label>
      <FormKit
        v-model="selectedFramework"
        data-test="frameworkSelector"
        type="select"
        name="frameworkSelector"
        :options="availableFrameworks"
        placeholder="Select framework"
        @input="resetErrors"
      />
      <p v-show="showFrameworksError" class="text-danger text-xs" data-test="frameworkError">
        Please select Framework.
      </p>
      <label for="reportingYearSelector">
        <b style="margin-bottom: 8px; font-weight: normal">Reporting year</b>
      </label>
      <div class="reporting-periods-selector flex flex-wrap gap-2 py-2">
        <ToggleChipFormInputs
          data-test="listOfReportingPeriods"
          class="toggle-chip-group"
          :options="dynamicReportingPeriods"
          :name="'listOfReportingPeriods'"
          @changed="resetErrors"
        />
      </div>
      <p v-if="showReportingPeriodsError" class="text-danger text-xs mt-2">
        Please select at least one reporting period.
      </p>
      <label for="fileTypeSelector">
        <b style="margin-bottom: 8px; margin-top: 5px; font-weight: normal">File Type</b>
      </label>
      <FormKit
        v-model="selectedFileType"
        data-test="fileTypeSelector"
        type="select"
        name="fileTypeSelector"
        :options="fileTypeSelectionOptions"
        placeholder="Select file type"
      />
      <p v-show="showFileTypeError" class="text-danger text-xs" data-test="fileTypeError">Please select a file type.</p>
      <FormKit
        v-model="valuesOnly"
        data-test="valuesCheckbox"
        class="valuesCheckbox"
        type="checkbox"
        name="valuesOnlyOption"
        label="Values only"
        help="Download only values without additional metadata"
      />
    </FormKit>
    <div class="download-section">
      <div class="error-message-container">
        <Message v-if="portfolioErrors" severity="error" class="m-0 error-message" :life="3000">
          {{ portfolioErrors }}
        </Message>
      </div>
      <div class="download-button-wrapper">
        <PrimeButton
          label="Download Portfolio"
          icon="pi pi-download"
          @click="onDownloadButtonClick"
          class="primary-button"
          :data-test="'downloadButton'"
          title="Download the selected frameworks and reporting periods for current portfolio"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject, onMounted, type Ref, ref } from 'vue';
import PrimeButton from 'primevue/button';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import { type EnrichedPortfolio, type EnrichedPortfolioEntry } from '@clients/userservice';
import { forceFileDownload } from '@/utils/FileDownloadUtils.ts';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { type CompanyIdAndName, ExportFileType } from '@clients/backend';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import type Keycloak from 'keycloak-js';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { AxiosError } from 'axios';
import Message from 'primevue/message';
const availableFrameworks = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'eutaxonomy-financials', label: 'EU Taxonomy Financials' },
  { value: 'eutaxonomy-non-financials', label: 'EU Taxonomy Non Financials' },
  { value: 'nuclear-and-gas', label: 'Nuclear and Gas' },
];

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const portfolioId = ref<string | undefined>(undefined);
const portfolioEntries = ref<EnrichedPortfolioEntry[]>([]);
const selectedFramework = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const valuesOnly = ref(true);
const showFileTypeError = ref(false);
const showReportingPeriodsError = ref(false);
const showFrameworksError = ref(false);
const portfolioErrors = ref('');
const selectedFileType = ref<'CSV' | 'EXCEL' | undefined>(undefined);
const fileTypeSelectionOptions = [
  { label: 'Comma-separated Values (.csv)', value: ExportFileType.Csv },
  { label: 'Excel-compatible CSV File (.csv)', value: ExportFileType.Excel },
];
const dynamicReportingPeriods = ref<{ name: string; value: boolean }[]>([
  { name: '2025', value: false },
  { name: '2024', value: false },
  { name: '2023', value: false },
  { name: '2022', value: false },
  { name: '2021', value: false },
  { name: '2020', value: false },
]);

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioId.value = portfolio.portfolioId;
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
 * Handle the clickEvent of the Download Button
 */
async function onDownloadButtonClick(): Promise<void> {
  checkIfShowErrors();

  if (showReportingPeriodsError.value || showFileTypeError.value) {
    return;
  }
  await downloadPortfolio();
}

/**
 * Extracts company IDs from selected portfolio
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
    if (!selectedFramework.value) {
      showFrameworksError.value = true;
      return;
    }

    if (!selectedFileType.value) {
      showFileTypeError.value = true;
      return;
    }

    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const frameworkDataApi = getFrameworkDataApiForIdentifier(
      selectedFramework.value,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const dataResponse = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
      getSelectedReportingPeriods(),
      getCompanyIds(),
      selectedFileType.value
    );

    const dataContent =
      selectedFileType.value === ExportFileType.Csv ? JSON.stringify(dataResponse.data) : dataResponse.data;

    const extension = selectedFileType.value === ExportFileType.Csv ? '.csv' : '.csv';
    const filename = 'portfolio-download' + extension;
    forceFileDownload(dataContent, filename);
  } catch (error) {
    if (error instanceof AxiosError) {
      portfolioErrors.value = error.status == 500 ? 'No data available' : error.message;
    }
    console.error('Download failed:', error);
  }
}
</script>

<style scoped lang="scss">
.portfolio-download-content {
  width: 20em;
  border-radius: 0.25rem;
  background-color: white;
  padding: 0.5rem 1.5rem;
  display: flex;
  flex-direction: column;
}

.download-section {
  height: 6rem;
  position: relative;
  margin-top: 1rem;
}

.reporting-periods-selector {
  margin-top: 0.5em;
  margin-bottom: 1em;
  min-height: 4em;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

label {
  display: block;
  margin-top: 10px;
  margin-bottom: 5px;
  font-weight: normal;
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

.valuesCheckbox .formkit-input[type='checkbox']:checked {
  background-color: #e67f3f;
  border-color: #e67f3f;
}

.formkit-wrapper {
  flex-grow: 1;
}

.error-message-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.error-message {
  width: 100%;
  padding: 1px;
  text-align: center;
  font-size: 0.875rem;
}

.download-button-wrapper {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
}
</style>
