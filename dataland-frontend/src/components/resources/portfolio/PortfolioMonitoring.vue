<template>
  <div class="portfolio-monitoring-content d-flex flex-column align-items-left">
    <label for="monitoringToggle" class="activate-monitoring"> Activate Monitoring </label>
    <InputSwitch class="form-field vertical-middle" v-model="monitoringActive" data-test="activateMonitoringToggle" />
    <label for="reportingYearSelector" class="reporting-period-label"> Starting Period </label>
    <Dropdown
      v-model="selectedStartingYear"
      :options="reportingYears"
      option-label="label"
      option-value="value"
      data-test="listOfReportingPeriods"
      placeholder="Select Starting Period"
      class="wider-dropdown"
      :disabled="!monitoringActive"
    />
    <p v-show="showReportingPeriodsError" class="text-danger" data-test="reportingPeriodsError">
      Please select Starting Period.
    </p>
    <label for="frameworkSelector"> Frameworks </label>
    <div class="framework-switch-group">
      <div
        v-for="framework in frameworkSwitchOptions"
        :key="framework.id"
        class="framework-switch-row"
        data-test="frameworkSelection"
      >
        <InputSwitch
          class="form-field vertical-middle"
          v-model="framework.value"
          :id="framework.id"
          @change="onFrameworksSwitched"
          :disabled="!monitoringActive"
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

    <div class="button-group-wrapper">
      <PrimeButton
        data-test="saveChangesButton"
        class="primary-button my-2 mr-2"
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
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { EU_TAXONOMY_FRAMEWORKS } from '@/utils/Constants.ts';
import Dropdown from 'primevue/dropdown';
import { sendBulkRequestForPortfolio } from '@/utils/RequestUtils.ts';
import { CompanyIdAndNameAndSector } from '@/types/CompanyTypes.ts';

const availableFrameworks: DropdownOption[] = [
  { value: 'sfdr', label: 'SFDR' },
  { value: 'eutaxonomy', label: 'EU Taxonomy' },
];
const reportingYears = [
  { label: '2024', value: 2024 },
  { label: '2023', value: 2023 },
  { label: '2022', value: 2022 },
  { label: '2021', value: 2021 },
  { label: '2020', value: 2020 },
  { label: '2019', value: 2019 },
];

const selectedStartingYear = ref<number | undefined>(undefined);

const portfolioCompanies = ref<CompanyIdAndNameAndSector[]>([]);
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const showFrameworksError = ref(false);
const showReportingPeriodsError = ref(false);
const portfolioId = ref<string>('');
const portfolioName = ref<string>('');
const userid = ref<string>('');

const frameworkSwitchOptions = reactive(
  availableFrameworks.map((framework) => ({
    id: framework.value,
    label: framework.label,
    value: false,
  }))
);

const monitoringActive = ref<boolean>(false);
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
    portfolioName.value = portfolio.portfolioName;
    userid.value = portfolio.userId;
    portfolioId.value = portfolio.portfolioId;
    portfolioCompanies.value = getUniqueSortedCompanies(
      portfolio.entries.map((entry) => new CompanyIdAndNameAndSector(entry))
    );
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
 * Retrieve the array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Handles creation of patch
 */
async function createPatch(): Promise<void> {
  const payloadPatchMonitoring: PortfolioMonitoringPatch = {
    isMonitored: true,
    startingMonitoringPeriod: selectedStartingYear.value as unknown as string,
    // as unknown as Set<string> cast required to ensure proper json is created
    monitoredFrameworks: selectedFrameworks.value as unknown as Set<string>,
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

  if (!monitoringActive.value) {
    const payloadPatchMonitoring: PortfolioMonitoringPatch = {
      isMonitored: false,
    };
    const portfolioControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
      .portfolioController;
    try {
      await portfolioControllerApi.patchMonitoring(portfolioId.value, payloadPatchMonitoring);
    } catch (error) {
      console.error('Error setting Monitoring Flag to false:', error);
    }

    dialogRef?.value.close({
      updated: true,
    });
  } else {

    if (!selectedStartingYear.value) {
      showReportingPeriodsError.value = true;
    }

    if (selectedFrameworks.value.length === 0) {
      showFrameworksError.value = true;
    }

    if (showReportingPeriodsError.value || showFrameworksError.value) {
      return;
    }

    try {
      await createPatch();

      await Promise.all(
        sendBulkRequestForPortfolio(
          selectedStartingYear.value! as unknown as string,
          Array.from(selectedFrameworks.value),
          portfolioCompanies.value,
          assertDefined(getKeycloakPromise)
        )
      );

      dialogRef?.value.close({
        updated: true,
      });
    } catch (error) {
      console.error('Error submitting Bulk Request for Portfolio Monitoring:', error);
    }
  }
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
    if (!portfolio.data.isMonitored) return;

    portfolioCompanies.value = getUniqueSortedCompanies(portfolio.data.entries);

    if (portfolio.data.startingMonitoringPeriod) {
      selectedStartingYear.value = Number(portfolio.data.startingMonitoringPeriod);
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

      monitoringActive.value = Boolean(portfolio.data.isMonitored);
    });
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
