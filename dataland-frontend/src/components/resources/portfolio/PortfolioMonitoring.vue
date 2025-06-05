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
    <p v-show="showReportingPeriodsError" class="text-danger" data-test="frameworkError">Please select Starting Period.</p>
    <label for="frameworkSelector">
      <div>Frameworks</div>
    </label>
    <div class="framework-switch-group">
      <div
        v-for="framework in frameworkSwitchOptions"
        :key="framework.id"
        class="framework-switch-row"
      >
        <InputSwitch
          class="form-field"
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
    <p v-show="showFrameworksError" class="text-danger" data-test="frameworkError">Please select at least one Framework.</p>
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
import type { EnrichedPortfolio, PortfolioMonitoringPatch } from '@clients/userservice';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { type BulkDataRequest, type BulkDataRequestDataTypesEnum } from '@clients/communitymanager';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import ToggleChipFormInputs from '@/components/general/ToggleChipFormInputs.vue';

const availableFrameworks: DropdownOption[] = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'EU_TAXONOMY', label: 'EU Taxonomy' },
];
const reportingYears = [2025, 2024, 2023, 2022, 2021, 2020];
const selectedStartingYear = ref<string | null>(null);
const selectedReportingPeriods = computed(() => {
  const startingYear = Number(selectedStartingYear.value);
  if (isNaN(startingYear)) return [];
  return reportingYears.filter((year) => year >= startingYear && year <= 2024).map(String);
});
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const portfolioId = ref<string | null>(null);
const reportingYearsToggleOptions = reactive(
  reportingYears.map((year) => ({
    name: String(year),
    value: false,
    tooltip: `Start monitoring from ${year}`
  }))
);
const showFrameworksError = ref(false);
const showReportingPeriodsError = ref(false);
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

const selectedFrameworks = computed(() =>
  frameworkSwitchOptions.filter((f) => f.value).map((f) => f.id)
);

/**
 * Resets Frameworkerros
 */
function onFrameworksSwitched(): void {
  resetErrors();
}

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioId.value = portfolio.portfolioId;
    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.entries);
  }
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
 * Handles creation of patch
 */
async function createPatch(): Promise<void> {
  let dataTypes: string[];
  if (selectedFrameworks.value.includes('EU_TAXONOMY')) {
    dataTypes = ['eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'];
  } else {
    dataTypes = selectedFrameworks.value;
  }
  const payloadPatchMonitoring: PortfolioMonitoringPatch = {
    isMonitored: true,
    startingMonitoringPeriod: selectedStartingYear.value as unknown as string,
    monitoredFrameworks: dataTypes as unknown as Set<string>,
  };
  const portfolioControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
    .portfolioController;

  try {
    await portfolioControllerApi.patchMonitoring(portfolioId.value as string, payloadPatchMonitoring);
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

  let dataTypes: string[];

  if (selectedFrameworks.value.includes('EU_TAXONOMY')) {
    dataTypes = ['eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'];
  } else {
    dataTypes = selectedFrameworks.value;
  }

  const payloadBulkDataRequest: BulkDataRequest = {
    reportingPeriods: selectedReportingPeriods.value as unknown as Set<string>,
    dataTypes: dataTypes as unknown as Set<BulkDataRequestDataTypesEnum>,
    companyIdentifiers: getCompanyIds() as unknown as Set<string>,
    notifyMeImmediately: false,
  };

  const requestDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
    .requestController;

  try {
    await requestDataControllerApi.postBulkDataRequest(payloadBulkDataRequest);
    await createPatch();

    dialogRef?.value.close({
      updated: true,
    });
  } catch (error) {
    console.error('Error submitting Bulk Request for Portfolio Monitoring:', error);
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
  justify-items: start;
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

.framework-label {
  margin: 0;
  cursor: pointer;
}

</style>
