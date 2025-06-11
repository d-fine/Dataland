<template>
  <div class="portfolio-monitoring-content d-flex flex-column align-items-left">
    <label for="monitoringToggle" class="activate-monitoring"> Activate Monitoring </label>
    <InputSwitch class="form-field vertical-middle" v-model="isMonitoringActive" data-test="activateMonitoringToggle" />
    <label for="reportingYearSelector" class="reporting-period-label"> Starting Period </label>
    <Dropdown
      v-model="selectedStartingYear"
      :options="reportingPeriodsOptions"
      option-label="label"
      option-value="value"
      data-test="listOfReportingPeriods"
      placeholder="Select Starting Period"
      class="wider-dropdown"
      :disabled="!isMonitoringActive"
    />
    <p v-show="showReportingPeriodsError" class="text-danger" data-test="reportingPeriodsError">
      Please select Starting Period.
    </p>
    <label for="frameworkSelector"> Frameworks </label>
    <div class="framework-switch-group">
      <div
        v-for="frameworkMonitoringOption in availableFrameworkMonitoringOptions"
        :key="frameworkMonitoringOption.value"
        class="framework-switch-row"
        data-test="frameworkSelection"
      >
        <InputSwitch
          class="form-field vertical-middle"
          v-model="frameworkMonitoringOption.isActive"
          :id="frameworkMonitoringOption.value"
          @change="resetErrors"
          :disabled="!isMonitoringActive"
        />
        <label :for="frameworkMonitoringOption.value" class="framework-label">
          {{ frameworkMonitoringOption.label }}
        </label>
      </div>
      <span class="gray-text font-italic text-xs ml-0 mb-3">
        EU Taxonomy creates requests for EU Taxonomy Financials, Non-Financials and Nuclear and Gas.
      </span>
    </div>
    <p v-show="showFrameworksError" class="text-danger" data-test="frameworkError">
      Please select at least one Framework.
    </p>

    <div class="button-group-wrapper">
      <PrimeButton
        data-test="saveChangesButton"
        class="primary-button my-2 mr-2"
        label="SAVE CHANGES"
        icon="pi pi-save"
        title="Cancel (changes will not be saved)"
        @click="patchPortfolioMonitoring()"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { CompanyIdAndNameAndSector, getUniqueSortedCompanies } from '@/types/CompanyTypes.ts';
import { sendBulkRequestsForPortfolio } from '@/utils/RequestUtils.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { EnrichedPortfolio, PortfolioMonitoringPatch } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import InputSwitch from 'primevue/inputswitch';
import { computed, inject, onMounted, type Ref, ref } from 'vue';

type MonitoringOption = {
  value: string;
  label: string;
  isActive: boolean;
};

const reportingYears = [2024, 2023, 2022, 2021, 2020, 2019];

const reportingPeriodsOptions = reportingYears.map((year) => ({
  label: year.toString(),
  value: year,
}));

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const portfolioControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
  .portfolioController;

const availableFrameworkMonitoringOptions = ref<MonitoringOption[]>([
  { value: 'sfdr', label: 'SFDR', isActive: false },
  { value: 'eutaxonomy', label: 'EU Taxonomy', isActive: false },
]);
const selectedStartingYear = ref<number | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndNameAndSector[]>([]);
const showFrameworksError = ref(false);
const showReportingPeriodsError = ref(false);
const portfolioId = ref<string>('');
const portfolioName = ref<string>('');
const userId = ref<string>('');

const isMonitoringActive = ref(false);

const selectedFrameworkOptions = computed(() => {
  return availableFrameworkMonitoringOptions.value.filter((option) => option.isActive).map((option) => option.value);
});

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    const portfolio = data.portfolio as EnrichedPortfolio;
    portfolioName.value = portfolio.portfolioName;
    userId.value = portfolio.userId;
    portfolioId.value = portfolio.portfolioId;
    portfolioCompanies.value = getUniqueSortedCompanies(
      portfolio.entries.map((entry) => new CompanyIdAndNameAndSector(entry))
    );
  }
  void prefillModal();
});

/**
 * Reset errors when either framework, reporting period or file type changes
 */
function resetErrors(): void {
  showReportingPeriodsError.value = false;
  showFrameworksError.value = false;
}

/**
 * Patch a portfolio with monitoring information
 */
async function patchPortfolioMonitoring(): Promise<void> {
  const portfolioMonitoringPatch: PortfolioMonitoringPatch = {
    isMonitored: isMonitoringActive.value,
    startingMonitoringPeriod: selectedStartingYear.value?.toString() ?? '',
    monitoredFrameworks: selectedFrameworkOptions.value as unknown as Set<string>,
  };

  if (!selectedStartingYear.value) {
    showReportingPeriodsError.value = true;
  }

  if (selectedFrameworkOptions.value.length === 0) {
    showFrameworksError.value = true;
  }

  if (showReportingPeriodsError.value || showFrameworksError.value) {
    return;
  }

  try {
    await portfolioControllerApi.patchMonitoring(portfolioId.value, portfolioMonitoringPatch);
    await Promise.all(
      sendBulkRequestsForPortfolio(
        selectedStartingYear.value! as unknown as string,
        Array.from(selectedFrameworkOptions.value),
        portfolioCompanies.value,
        assertDefined(getKeycloakPromise)
      )
    );

    dialogRef?.value.close();
  } catch (error) {
    console.error('Error submitting Monitoring Patch for Portfolio:', error);
  }
}

/**
 * Prefills Modal based on database
 */
async function prefillModal(): Promise<void> {
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const portfolio = await apiClientProvider.apiClients.portfolioController.getEnrichedPortfolio(portfolioId.value);

    if (!portfolio) return;

    const portfolioData = portfolio.data;

    isMonitoringActive.value = portfolioData.isMonitored ?? false;

    if (!isMonitoringActive.value) {
      selectedStartingYear.value = undefined;
      availableFrameworkMonitoringOptions.value = availableFrameworkMonitoringOptions.value.map((opt) => ({
        ...opt,
        isActive: false,
      }));
      return;
    }

    if (portfolioData.startingMonitoringPeriod) {
      selectedStartingYear.value = Number(portfolioData.startingMonitoringPeriod);
    }

    const monitoredSet = new Set(portfolioData.monitoredFrameworks);
    availableFrameworkMonitoringOptions.value = availableFrameworkMonitoringOptions.value.map((opt) => ({
      ...opt,
      isActive: monitoredSet.has(opt.value),
    }));

    portfolioCompanies.value = getUniqueSortedCompanies(portfolioData.entries);
  } catch (error) {
    console.error('Error fetching and prefilling enriched portfolio:', error);
  }
}
</script>

<style scoped lang="scss">
.button-group-wrapper {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
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

.wider-dropdown {
  width: 250px;
}
</style>
