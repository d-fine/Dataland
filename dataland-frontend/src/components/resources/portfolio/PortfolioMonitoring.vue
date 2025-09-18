<template>
  <div class="portfolio-monitoring-content d-flex flex-column align-items-left">
    <div>
      <p class="header-styling">Activate Monitoring</p>
      <ToggleSwitch
        class="form-field vertical-middle"
        v-model="isMonitoringActive"
        data-test="activateMonitoringToggle"
        @update:modelValue="onMonitoringToggled"
      />
    </div>
    <div>
      <p class="header-styling">Starting Period</p>
      <Select
        v-model="selectedStartingYear"
        :options="reportingPeriodsOptions"
        option-label="label"
        option-value="value"
        data-test="listOfReportingPeriods"
        placeholder="Select Starting Period"
        :disabled="!isMonitoringActive"
        @change="resetErrors"
        :highlightOnSelect="false"
        fluid
      />
      <Message
        v-if="showReportingPeriodsError"
        severity="error"
        variant="simple"
        size="small"
        data-test="reportingPeriodsError"
      >
        Please select Starting Period.
      </Message>
    </div>
    <div>
      <p class="header-styling">Frameworks</p>
      <div class="framework-switch-group">
        <div
          v-for="frameworkMonitoringOption in availableFrameworkMonitoringOptions"
          :key="frameworkMonitoringOption.value"
          data-test="frameworkSelection"
        >
          <div class="framework-toggle-label">
            <ToggleSwitch
              v-model="frameworkMonitoringOption.isActive"
              class="form-field vertical-middle"
              data-test="valuesOnlySwitch"
              @change="resetErrors"
              :disabled="!isMonitoringActive"
            />
            <p :for="frameworkMonitoringOption.value" class="">
              {{ frameworkMonitoringOption.label }}
            </p>
          </div>
        </div>
        <div class="dataland-info-text small">
          EU Taxonomy creates requests for EU Taxonomy Financials, Non-Financials and Nuclear and Gas.
        </div>
      </div>
    </div>
    <Message v-if="showFrameworksError" severity="error" variant="simple" size="small" data-test="frameworkError">
      Please select at least one Framework.
    </Message>
    <div class="button-wrapper">
      <PrimeButton
        type="button"
        data-test="saveChangesButton"
        label="SAVE CHANGES"
        icon="pi pi-save"
        @click="patchPortfolioMonitoring()"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { EnrichedPortfolio, PortfolioMonitoringPatch } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { computed, inject, onMounted, type Ref, ref } from 'vue';
import ToggleSwitch from 'primevue/toggleswitch';
import Message from 'primevue/message';
import { PORTFOLIO_MONITORING_REPORTING_PERIODS } from '@/utils/Constants.ts';

type MonitoringOption = {
  value: string;
  label: string;
  isActive: boolean;
};

const reportingPeriodsOptions = PORTFOLIO_MONITORING_REPORTING_PERIODS.map((yearString) => ({
  label: yearString,
  value: +yearString,
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
const showFrameworksError = ref(false);
const showReportingPeriodsError = ref(false);
const portfolio = ref<EnrichedPortfolio>();
const isMonitoringActive = ref(false);
const previousStartingYear = ref<number | undefined>(undefined);
const previousFrameworks = ref<Set<string>>(new Set());

const selectedFrameworkOptions = computed(() => {
  return availableFrameworkMonitoringOptions.value.filter((option) => option.isActive).map((option) => option.value);
});

onMounted(() => {
  const data = dialogRef?.value.data;
  if (data?.portfolio) {
    portfolio.value = data.portfolio as EnrichedPortfolio;
    prefillModal();
  } else {
    dialogRef?.value.close();
  }
});

/**
 * Handles toggling of the monitoring switch.
 * Stores and restores selections when toggled on/off.
 *
 * @param newValue changed monitoring state.
 */
function onMonitoringToggled(newValue: boolean): void {
  if (!newValue) {
    previousStartingYear.value = selectedStartingYear.value;
    previousFrameworks.value = new Set(
      availableFrameworkMonitoringOptions.value.filter((option) => option.isActive).map((option) => option.value)
    );

    selectedStartingYear.value = undefined;
    availableFrameworkMonitoringOptions.value = availableFrameworkMonitoringOptions.value.map((option) => ({
      ...option,
      isActive: false,
    }));

    resetErrors();
  } else {
    selectedStartingYear.value = previousStartingYear.value;
    availableFrameworkMonitoringOptions.value = availableFrameworkMonitoringOptions.value.map((option) => ({
      ...option,
      isActive: previousFrameworks.value.has(option.value),
    }));
  }
}

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

  if (isMonitoringActive.value) {
    showReportingPeriodsError.value = !selectedStartingYear.value;
    showFrameworksError.value = selectedFrameworkOptions.value.length === 0;

    if (showReportingPeriodsError.value || showFrameworksError.value) {
      return;
    }
  } else {
    resetErrors();
  }

  try {
    await portfolioControllerApi.patchMonitoring(portfolio.value!.portfolioId, portfolioMonitoringPatch);
    dialogRef?.value.close();
  } catch (error) {
    console.error('Error submitting Monitoring Patch for Portfolio:', error);
  }
}

/**
 * Prefills Modal based on database
 */
function prefillModal(): void {
  if (!portfolio.value) return;

  isMonitoringActive.value = portfolio.value.isMonitored ?? false;

  if (!isMonitoringActive.value) {
    selectedStartingYear.value = undefined;
    availableFrameworkMonitoringOptions.value = availableFrameworkMonitoringOptions.value.map((option) => ({
      ...option,
      isActive: false,
    }));
    return;
  }

  if (portfolio.value.startingMonitoringPeriod) {
    selectedStartingYear.value = Number(portfolio.value.startingMonitoringPeriod);
  }

  const monitoredFrameworksRaw = portfolio.value.monitoredFrameworks as string[] | undefined;
  const monitoredFrameworks = new Set(monitoredFrameworksRaw ?? []);

  availableFrameworkMonitoringOptions.value = availableFrameworkMonitoringOptions.value.map((option) => ({
    ...option,
    isActive: monitoredFrameworks.has(option.value),
  }));
}
</script>

<style scoped>
.header-styling {
  font-weight: var(--font-weight-semibold);
}

.button-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-xs);
}

.portfolio-monitoring-content {
  width: 20rem;
  height: 100%;
  border-radius: var(--spacing-xxs);
  background-color: white;
  padding: var(--spacing-xs) var(--spacing-lg);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.framework-switch-group {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.framework-toggle-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}
</style>
