<template>
  <div class="portfolio-download-content">
    <BasicFormSection header="Select framework">
      <Dropdown
        name="FrameworkSelection"
        placeholder="Please select"
        :options="availableFrameworks"
        optionValue="value"
        optionLabel="label"
        v-model="selectedFramework"
        class="long"
        @change="onFrameworkChange"
      />
    </BasicFormSection>
    <div class="select-group">
      <BasicFormSection header="Select Reporting Period(s)">
        <div class="flex flex-wrap mt-4 py-2">
          <template v-if="dynamicReportingPeriods.length > 0">
            <ToggleChipFormInputs
              :name="'listOfReportingPeriods'"
              :options="dynamicReportingPeriods"
              @changed="selectedReportingPeriodsError = false"
            />
          </template>
          <template v-else>
            <p class="gray-text font-italic text-xs m-0">
              Selection of reporting periods is available upon framework selection.
            </p>
          </template>
        </div>
        <p v-if="selectedReportingPeriodsError" class="text-danger text-xs mt-2">
          Select at least one reporting period.
        </p>
      </BasicFormSection>
      <BasicFormSection header="File type">
        <template v-if="selectedFramework">
          <Dropdown
            name="fileType"
            v-model="selectedFileType"
            :options="fileTypeOptions"
            optionValue="value"
            optionLabel="label"
            placeholder="Select"
            class="long"
          />
        </template>
        <template v-else>
          <p class="gray-text font-italic text-xs m-0">
            Selection of reporting periods is available upon framework selection.
          </p>
        </template>
      </BasicFormSection>
      <PrimeButton
        label="Download Portfolio"
        icon="pi pi-download"
        @click="downloadPortfolio()"
        class="primary-button downloadButton"
        :data-test="'downloadButton'"
        title="Download the selected frameworks and reporting periods for current portfolio"
        style="width: max-content"
        :disabled="dynamicReportingPeriods.length === 0"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject, onMounted, type Ref, ref } from 'vue';
import PrimeButton from 'primevue/button';
import BasicFormSection from '@/components/general/BasicFormSection.vue';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import { type CompanyIdAndName, ExportFileType } from '@clients/backend';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { type EnrichedPortfolio, type EnrichedPortfolioEntry } from '@clients/userservice';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import type Keycloak from 'keycloak-js';
import Dropdown from 'primevue/dropdown';
import { forceFileDownload } from '@/utils/FileDownloadUtils.ts';
const dynamicReportingPeriods = ref<{ name: string; value: boolean }[]>([]);
const availableFrameworks = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'eutaxonomy-financials', label: 'EU Taxonomy Financials' },
  { value: 'eutaxonomy-non-financials', label: 'EU Taxonomy Non-Financials' },
  { value: 'nuclear-and-gas', label: 'Nuclear and Gas' },
];

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');

const portfolioId = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const selectedReportingPeriodsError = ref(false);
const selectedFramework = ref<string | null>(null);
const portfolioEntries = ref<EnrichedPortfolioEntry[]>([]);
const selectedFileType = ref<ExportFileType | null>(null);
const fileTypeOptions = [
  { label: 'CSV', value: ExportFileType.Csv },
  { label: 'Excel', value: ExportFileType.Excel },
];

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioId.value = portfolio.portfolioId;
    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.entries);
    portfolioEntries.value = portfolio.entries;
  }
});

/**
 * When the framework changes, update the available reporting periods based on the selected framework
 */
function onFrameworkChange(): void {
  setReportingPeriods(portfolioEntries.value);
}

/**
 * Set available reporting periods based on the selected framework
 */
function setReportingPeriods(entries: EnrichedPortfolioEntry[]): void {
  if (!selectedFramework.value) return;

  const periodsSet = new Set<string>();
  entries.forEach((entry) => {
    if (entry.framework === selectedFramework.value && entry.latestReportingPeriod) {
      periodsSet.add(entry.latestReportingPeriod);
    }
  });
  const sortedPeriods = Array.from(periodsSet).sort((a, b) => b.localeCompare(a));

  dynamicReportingPeriods.value = sortedPeriods.map((period) => ({
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
 * Extracts company IDs from selected portfolio
 */
function getCompanyIds(): string[] {
  return portfolioCompanies.value.map((company) => company.companyId);
}

/**
 * Retrieve array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Download portfolio with the selected reporting period as a file in the selected format
 */
async function downloadPortfolio(): Promise<void> {
  try {

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
    const filename = `portfolio-download`;
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

.portfolio-download-content {
  width: 28em;
  border-radius: 0.25rem;
  background-color: white;
  padding: 1.5rem;
}

.portfolio-dialog-content {
  width: 28em;
  border-radius: 0.25rem;
  background-color: white;
  padding: 1.5rem;
}

.framework-pill-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.form-list-item {
  background-color: #f0f0f0;
  border-radius: 20px;
  padding: 0.3rem 0.75rem;
  display: flex;
  align-items: center;
  font-size: 0.875rem;
}

.form-list-item em {
  font-style: normal;
  margin-left: 0.5rem;
  cursor: pointer;
  color: #888;
}

.no-framework {
  color: #aaa;
  font-style: italic;
}
.buttonbar {
  display: flex;
  gap: 1rem;
  margin-top: 1em;
  margin-left: auto;
  justify-content: end;
}

label {
  margin-top: 1.5em;
}

ul {
  padding-inline-start: 0;
  height: 7.5em;
  overflow-x: hidden;
  overflow-y: auto;
}

.downloadButton {
  min-width: fit-content;
  padding: 1em;
}

:deep(.basic-form-section) {
  margin-bottom: 0.5rem !important;
  padding-bottom: 0 !important;
}
</style>
