<template>
  <div class="portfolio-monitoring-content d-flex flex-column align-items-left">
    <label for="reportingYearSelector" class="reporting-period-label">
      <div>Starting Period</div>
    </label>
    <ToggleChipFormInputs
      :name="'listOfReportingPeriods'"
      :options="reportingYearsToggleOptions"
      :availableOptions="availableReportingPeriods"
      @changed="onReportingYearsChanged"
      data-test="listOfReportingPeriods"
      class="toggle-chip-group"
    />
    <p v-show="showReportingPeriodsError" class="text-danger" data-test="frameworkError">
      Please select Starting Period.
    </p>
    <label for="frameworkSelector">
      <div>Frameworks</div>
    </label>
    <div class="framework-switch-group">
      <div v-for="framework in frameworkSwitchOptions" :key="framework.id" class="framework-switch-row">
        <InputSwitch
          class="form-field vertical-middle"
          v-model="framework.value"
          :id="framework.id"
          @change="onFrameworksSwitched"
        />
        <label :for="framework.id" class="framework-label">
          {{ framework.label }}
        </label>
      </div>
      <span class="gray-text font-italic text-xs ml-0 mb-3">
        EU Taxonomy creates requests for EU Taxonomy Financials, Non-Financials and Nuclear and Gas.
      </span>
    </div>
    <p v-show="showFrameworksError" class="text-danger" data-test="frameworkError">
      Please select at least one Framework.
    </p>
    <div class="button-wrapper" style="width: 100%; text-align: right">
      <PrimeButton
        data-test="saveChangesButton"
        class="primary-button my-2"
        label="SAVE CHANGES"
        icon="pi pi-save"
        title="Cancel (changes will not be saved)"
        @click="createBulkDataRequest()"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { DropdownOption } from '@/utils/PremadeDropdownDatasets.ts';
import InputSwitch from 'primevue/inputswitch';
import { computed, inject, onMounted, reactive, type Ref, ref } from 'vue';
import PrimeButton from 'primevue/button';
import type { CompanyIdAndName } from '@clients/backend';
import type { EnrichedPortfolio, EnrichedPortfolioEntry, PortfolioMonitoringPatch } from '@clients/userservice';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { type BulkDataRequest, type BulkDataRequestDataTypesEnum } from '@clients/communitymanager';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';
import { EU_TAXONOMY_FRAMEWORKS } from '@/utils/Constants.ts';

const availableFrameworks: DropdownOption[] = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'EU_TAXONOMY', label: 'EU Taxonomy' },
];
const reportingYears = [2024, 2023, 2022, 2021, 2020, 2019];
const selectedStartingYear = ref<string | null>(null);
const selectedReportingPeriods = computed(() => {
  const startingYear = Number(selectedStartingYear.value);
  if (isNaN(startingYear)) return [];
  return reportingYears.filter((year) => year >= startingYear && year <= 2024).map(String);
});
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const reportingYearsToggleOptions = reactive(
  reportingYears.map((year) => ({
    name: String(year),
    value: false,
    tooltip: `Start monitoring from ${year}`,
  }))
);
const showFrameworksError = ref(false);
const showReportingPeriodsError = ref(false);
const portfolioId = ref<string>('');
const portfolioEntries = ref<EnrichedPortfolioEntry[]>([]);
const availableReportingPeriods = computed(() => {
  if (!selectedStartingYear.value) {
    return reportingYearsToggleOptions;
  }

  return reportingYearsToggleOptions.filter((option) => option.name === selectedStartingYear.value);
});

const frameworkSwitchOptions = reactive(
  availableFrameworks.map((framework) => ({
    id: framework.value,
    label: framework.label,
    value: false,
  }))
);

const selectedFrameworks = computed(() => frameworkSwitchOptions.filter((f) => f.value).map((f) => f.id));

/**
 * Resets Frameworkerros
 */
function onFrameworksSwitched(): void {
  resetErrors();
}

onMounted(async () => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.entries);
    portfolioId.value = portfolio.portfolioId;
    portfolioEntries.value = portfolio.entries;
  }
  await prefillModal();
});

/**
 * Reset errors when either framework, reporting period or file type changes
 */
function resetErrors(): void {
  showReportingPeriodsError.value = false;
  showFrameworksError.value = false;
}

/**
 * Handles change of startingYear
 */
function onReportingYearsChanged(): void {
  const selected = reportingYearsToggleOptions.find((option) => option.value);

  if (selected) {
    selectedStartingYear.value = selected.name;
    reportingYearsToggleOptions.forEach((option) => {
      option.value = option.name === selected.name;
    });
  } else {
    selectedStartingYear.value = null;
    reportingYearsToggleOptions.forEach((option) => {
      option.value = false;
    });
  }
}

/**
 * Retrieve the array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Extracts company IDs from the selected portfolio
 */
function getCompanyIds(): string[] {
  return portfolioCompanies.value.map((company) => company.companyId);
}

/**
 * Gets datatypes for Framework selection
 * @param frameworks selected frameworks of the user
 */
function getDataTypesFromFrameworks(frameworks: string[]): string[] {
  const dataTypes: string[] = [];

  if (frameworks.includes('EU_TAXONOMY')) {
    dataTypes.push('eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas');
  }
  frameworks.forEach((fw) => {
    if (fw !== 'EU_TAXONOMY') dataTypes.push(fw);
  });

  return dataTypes;
}

/**
 * Handles creation of patch
 */
async function createPatch(): Promise<void> {
  const dataTypes = getDataTypesFromFrameworks(selectedFrameworks.value);
  const payloadPatchMonitoring: PortfolioMonitoringPatch = {
    isMonitored: true,
    startingMonitoringPeriod: selectedStartingYear.value as unknown as string,
    monitoredFrameworks: dataTypes as unknown as Set<string>,
  };
  const portfolioControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
    .portfolioController;

  try {
    await portfolioControllerApi.patchMonitoring(portfolioId.value, payloadPatchMonitoring);
  } catch (error) {
    console.error('Error submitting Monitoring Patch for Portfolio:', error);
  }
}

/**
 * Handles Bulk Request for Portfolio Monitoring
 */
async function createBulkDataRequest(): Promise<void> {
  resetErrors();

  if (!selectedStartingYear.value) {
    showReportingPeriodsError.value = true;
  }

  if (selectedFrameworks.value.length === 0) {
    showFrameworksError.value = true;
  }

  if (showReportingPeriodsError.value || showFrameworksError.value) {
    return;
  }

  const isEUTaxonomySelected = selectedFrameworks.value.includes('EU_TAXONOMY');
  const isSfdrSelected = selectedFrameworks.value.includes('sfdr');

  const financialCompanyIds = portfolioEntries.value
    .filter((c) => c.sector?.toLowerCase() === 'financials')
    .map((c) => c.companyId);

  const nonFinancialCompanyIds = portfolioEntries.value
    .filter((c) => c.sector?.toLowerCase() !== 'financials')
    .map((c) => c.companyId);


  const requests = [];
  const reportingPeriodsSet = selectedReportingPeriods.value as unknown as Set<string>;


  if (isEUTaxonomySelected && financialCompanyIds.length > 0) {
    requests.push(
      sendBulkRequest(
        reportingPeriodsSet,
        new Set(['eutaxonomy-financials', 'nuclear-and-gas']),
        new Set(financialCompanyIds)
      )
    );
  }

  if (isEUTaxonomySelected && nonFinancialCompanyIds.length > 0) {
    requests.push(
      sendBulkRequest(
        reportingPeriodsSet,
        new Set(['eutaxonomy-non-financials', 'nuclear-and-gas']),
        new Set(nonFinancialCompanyIds)
      )
    );
  }

  if (isSfdrSelected) {
    requests.push(
      sendBulkRequest(
        reportingPeriodsSet,
        new Set(['sfdr']),
        new Set(getCompanyIds())
      )
    );
  }

  try {
    await Promise.all(requests);
    await createPatch();

    dialogRef?.value.close({
      updated: true,
    });
  } catch (error) {
    console.error('Error submitting Bulk Request for Portfolio Monitoring:', error);
  }
}

/**
 * Sends bulk data request
 * @param reportingPeriods
 * @param dataTypes
 * @param companyIdentifiers
 */
async function sendBulkRequest(
  reportingPeriods: Set<string>,
  dataTypes: Set<BulkDataRequestDataTypesEnum>,
  companyIdentifiers: Set<string>
): Promise<void> {
  const payloadBulkDataRequest: BulkDataRequest = {
    reportingPeriods: Array.from(reportingPeriods) as unknown as Set<string>,
    dataTypes: Array.from(dataTypes) as unknown as Set<BulkDataRequestDataTypesEnum>,
    companyIdentifiers: Array.from(companyIdentifiers) as unknown as Set<string>,
    notifyMeImmediately: false,
  };
  console.log(payloadBulkDataRequest)

  const requestDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
    .requestController;

  await requestDataControllerApi.postBulkDataRequest(payloadBulkDataRequest);
}

/**
 * Prefills Modal based on database
 */
async function prefillModal(): Promise<void> {
  if (!portfolioId.value) return;

  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const portfolio = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(portfolioId.value);

    if (!portfolio) return;

    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.data.entries);

    if (portfolio.data.startingMonitoringPeriod) {
      const startPeriod = String(portfolio.data.startingMonitoringPeriod);
      selectedStartingYear.value = startPeriod;

      reportingYearsToggleOptions.forEach((option) => {
        option.value = option.name === startPeriod;
      });
    }

    let monitoredSet: Set<string>;
    if (portfolio.data.monitoredFrameworks instanceof Set) {
      monitoredSet = portfolio.data.monitoredFrameworks;
    } else if (Array.isArray(portfolio.data.monitoredFrameworks)) {
      monitoredSet = new Set(portfolio.data.monitoredFrameworks);
    } else {
      monitoredSet = new Set();
    }

    const euTaxoDatatypes = new Set(EU_TAXONOMY_FRAMEWORKS);
    const hasEuTaxoDatatypes = [...monitoredSet].some((dt) => euTaxoDatatypes.has(dt));

    frameworkSwitchOptions.forEach((option) => {
      if (option.id === 'EU_TAXONOMY') {
        option.value = hasEuTaxoDatatypes || monitoredSet.has(option.id);
      } else {
        option.value = monitoredSet.has(option.id);
      }
    });
  } catch (error) {
    console.error('Error fetching and prefilling enriched portfolio:', error);
  }
}
</script>

<style scoped lang="scss">
.portfolio-monitoring-content {
  width: 20em;
  height: 100%;
  border-radius: 0.25rem;
  background-color: white;
  padding: 0.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start; /* ensure container children align left */
}

label {
  width: 100%;
  margin-bottom: 1em;
  padding: 0;
}

label > div {
  text-align: left;
}

.toggle-chip-group,
.framework-select-wrapper {
  width: 100%;
}

.toggle-chip-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  justify-items: center;
  justify-content: center;
  margin-bottom: 1.5em;
}

.button-wrapper {
  width: 100%;
  text-align: right;
}

.framework-switch-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.framework-switch-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.framework-switch-row :deep(.p-inputswitch) {
  width: 2.6rem;
  height: 1.2rem;
}

.framework-switch-row :deep(.p-inputswitch-slider) {
  height: 100%;
  border-radius: 1rem;
}

.framework-label {
  margin: 0;
  cursor: pointer;
}
</style>
