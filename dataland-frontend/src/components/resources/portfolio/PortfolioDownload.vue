<template>
  <div class="portfolio-download-content">
    <FormKit type="form" class="formkit-wrapper" :actions="false">
      <label for="fileTypeSelector">
        <b style="margin-bottom: 8px; margin-top: 5px; font-weight: normal">Framework</b>
      </label>
      <FormKit
        v-model="selectedFramework"
        type="select"
        name="fileTypeSelector"
        data-test="fileTypeSelector"
        :options="availableFrameworks"
        placeholder="Select a file type"
      />
      <p v-show="showFileTypeError" class="text-danger text-xs" data-test="fileTypeError">Please select a file type.</p>

      <label for="reportingYearSelector">
        <b style="margin-bottom: 8px; font-weight: normal">Reporting year</b>
      </label>
      <div class="reporting-periods-selector">
        <template v-if="selectedFramework">
          <div v-if="dynamicReportingPeriods.length > 0" class="flex flex-wrap py-2">
            <ToggleChipFormInputs
              :name="'listOfReportingPeriods'"
              :options="dynamicReportingPeriods"
              @changed="showReportingPeriodsError = false"
            />
          </div>
          <p v-else class="text-sm text-danger mt-2">
            No reporting periods available for {{ getFrameworkLabel(selectedFramework) }} and selected Portfolio.
          </p>
        </template>
        <template v-else>
          <p class="gray-text font-italic text-xs m-0">
            Selection of reporting periods is available upon framework selection.
          </p>
        </template>
      </div>
      <p v-if="showReportingPeriodsError" class="text-danger text-xs mt-2">Select at least one reporting period.</p>

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
    <PrimeButton
      label="Download Portfolio"
      icon="pi pi-download"
      @click="onDownloadButtonClick"
      class="primary-button downloadButton"
      :data-test="'downloadButton'"
      title="Download the selected frameworks and reporting periods for current portfolio"
      style="width: max-content"
    />
  </div>
</template>

<script setup lang="ts">
import { inject, onMounted, type Ref, ref, watch } from 'vue';
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

const dynamicReportingPeriods = ref<{ name: string; value: boolean }[]>([]);
const availableFrameworks = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'eutaxonomy-financials', label: 'EU Taxonomy Non-Financials' },
  { value: 'eutaxonomy-non-financials', label: 'EU Taxonomy Financials' },
  { value: 'nuclear-and-gas', label: 'Nuclear and Gas' },
];

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const portfolioId = ref<string | undefined>(undefined);
const portfolioEntries = ref<EnrichedPortfolioEntry[]>([]);
const selectedFramework = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const showFileTypeError = ref(false);
const showReportingPeriodsError = ref(false);
const selectedFileType = ref<'CSV' | 'EXCEL' | undefined>(undefined);
const fileTypeSelectionOptions = [
  { label: 'Comma-separated Values (.csv)', value: ExportFileType.Csv },
  { label: 'Excel-compatible CSV File (.csv)', value: ExportFileType.Excel },
];

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioId.value = portfolio.portfolioId;
    portfolioEntries.value = portfolio.entries;
  }
});

watch(selectedFramework, () => {
  onFrameworkChange();
});

/**
 * Retrieves currently selected framework
 */
function getFrameworkLabel(value: string | undefined): string {
  const framework = availableFrameworks.find((fw) => fw.value === value);
  return framework?.label ?? value ?? 'selected framework';
}

/**
 * When the framework changes, update the available reporting periods based on the selected framework
 */
function onFrameworkChange(): void {
  setReportingPeriods(portfolioEntries.value);
}

/**
 * When the framework changes, update the available reporting periods based on the selected framework
 */
function setReportingPeriods(entries: EnrichedPortfolioEntry[]): void {
  if (!selectedFramework.value) return;

  const periodsSet = new Set<string>();
  entries.forEach((entry) => {
    if (entry.framework === selectedFramework.value && entry.latestReportingPeriod) {
      periodsSet.add(entry.latestReportingPeriod);
    }
  });

  dynamicReportingPeriods.value = Array.from(periodsSet)
    .sort((a, b) => parseInt(b) - parseInt(a))
    .map((period) => ({
      name: period,
      value: false,
    }));
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
}

/**
 * Handles download of portfolio
 */
async function downloadPortfolio(): Promise<void> {
  try {
    if (!selectedFramework.value) {
      alert('Please select a framework.');
      return;
    }

    if (!selectedFileType.value) {
      alert('Please select a file type.');
      return;
    }

    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const frameworkDataApi = getFrameworkDataApiForIdentifier(
      selectedFramework.value,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;
    const selectedPeriods = getSelectedReportingPeriods();

    if (!selectedPeriods.values) {
      alert('Please select at least one reporting period.');
      return;
    }

    const companyIds = getCompanyIds();
    const filename = 'portfolio-download';
    const dataResponse = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
      selectedPeriods,
      companyIds,
      selectedFileType.value
    );

    const dataContent =
      selectedFileType.value === ExportFileType.Csv ? JSON.stringify(dataResponse.data) : dataResponse.data;

    forceFileDownload(dataContent, filename);
  } catch (error) {
    console.error('Download failed:', error);
    alert('Download failed');
  }
}
</script>

<style scoped lang="scss">
.downloadButton {
  width: max-content;
  margin-left: auto;
  justify-content: end;
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

.portfolio-download-content {
  width: 20em;
  border-radius: 0.25rem;
  background-color: white;
  padding: 1.5rem;
}

.errorMessage {
  gap: 10em;
}

.text-danger {
  color: red;
}
</style>
