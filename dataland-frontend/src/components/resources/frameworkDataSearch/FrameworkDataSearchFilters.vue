<template>
  <div class="filter-container">
    <div class="filter">
      <div class="filter-group">
        <label for="sector-filter" v-if="showHeading">Filter by company</label>
        <div class="filter-group-items">
          <FrameworkDataSearchDropdownFilter
            v-model="localSelectedSectors"
            ref="sectorFilter"
            :available-items="availableSectors"
            filter-name="Sector"
            id="sector-filter"
            filter-placeholder="Search sectors"
            :max-selected-labels="1"
            selected-items-label="{0} sectors"
            class="search-filter"
            data-test="frameworkDataSearchDropdownFilterSector"
          />
          <FrameworkDataSearchDropdownFilter
            v-model="localSelectedCountries"
            ref="countryFilter"
            :available-items="availableCountries"
            filter-name="Country"
            id="country-filter"
            filter-placeholder="Search countries"
            :max-selected-labels="1"
            selected-items-label="{0} countries"
            class="search-filter"
          />
        </div>
      </div>
      <Divider layout="vertical" class="filter-divider" />
      <div class="filter-group">
        <label for="framework-filter" v-if="showHeading">Filter for available data sets</label>
        <div class="filter-group-items">
          <FrameworkDataSearchDropdownFilter
            v-model="localSelectedFrameworks"
            ref="frameworkFilter"
            :available-items="availableFrameworks"
            filter-name="Framework"
            id="framework-filter"
            filter-placeholder="Search frameworks"
            selected-items-label="{0} frameworks"
            :max-selected-labels="1"
            class="search-filter framework-filter"
          />
          <FrameworkDataSearchDropdownFilter
            v-model="localSelectedReportingPeriods"
            ref="reportingPeriodFilter"
            :available-items="availableReportingPeriods"
            filter-name="Reporting Period"
            id="reporting-period-filter"
            filter-placeholder="Search reporting periods"
            selected-items-label="{0} reporting periods"
            :max-selected-labels="1"
            class="search-filter"
            data-test="frameworkDataSearchDropdownFilterReportingPeriod"
          />
        </div>
      </div>
    </div>
    <PrimeButton variant="link" @click="resetFilters" label="RESET" data-test="reset-filter" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import {
  type CountryCodeSelectableItem,
  type FrameworkSelectableItem,
  type SelectableItem,
} from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import { useApiClient } from '@/utils/useApiClient';
import PrimeButton from 'primevue/button';
import Divider from 'primevue/divider';
import { type DataTypeEnum } from '@clients/backend';

export interface FrameworkDataSearchDropdownFilterRef {
  multiselect?: {
    hide?: () => void;
  } | null;
}

const props = withDefaults(
  defineProps<{
    selectedFrameworks?: Array<DataTypeEnum>;
    selectedSectors?: Array<string>;
    selectedCountryCodes?: Array<string>;
    selectedReportingPeriods?: Array<string>;
    showHeading?: boolean;
  }>(),
  {
    selectedFrameworks: () => [],
    selectedSectors: () => [],
    selectedCountryCodes: () => [],
    selectedReportingPeriods: () => [],
    showHeading: true,
  }
);

const emit = defineEmits<{
  'update:selectedCountryCodes': [selectedCountryCodes: Array<string>];
  'update:selectedFrameworks': [selectedFrameworks: Array<DataTypeEnum>];
  'update:selectedSectors': [selectedSectors: Array<string>];
  'update:selectedReportingPeriods': [selectedReportingPeriods: Array<string>];
}>();

const sectorFilter = ref<FrameworkDataSearchDropdownFilterRef | null>(null);
const countryFilter = ref<FrameworkDataSearchDropdownFilterRef | null>(null);
const frameworkFilter = ref<FrameworkDataSearchDropdownFilterRef | null>(null);
const reportingPeriodFilter = ref<FrameworkDataSearchDropdownFilterRef | null>(null);
const apiClientProvider = useApiClient();

const localSelectedCountries = ref<Array<CountryCodeSelectableItem>>([]);
const localSelectedFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const localSelectedSectors = ref<Array<SelectableItem>>([]);
const localSelectedReportingPeriods = ref<Array<SelectableItem>>([]);

const availableCountries = ref<Array<CountryCodeSelectableItem>>([]);
const availableFrameworks = ref<Array<FrameworkSelectableItem>>([]);
const availableSectors = ref<Array<SelectableItem>>([]);
const availableReportingPeriods = ref<Array<SelectableItem>>([]);

watch(
  localSelectedCountries,
  (newValue) => {
    emit(
      'update:selectedCountryCodes',
      newValue.map((item) => item.countryCode)
    );
  },
  { deep: true }
);

watch(
  localSelectedSectors,
  (newValue) => {
    emit(
      'update:selectedSectors',
      newValue.map((item) => item.displayName)
    );
  },
  { deep: true }
);

watch(
  localSelectedFrameworks,
  (newValue) => {
    emit(
      'update:selectedFrameworks',
      newValue.map((item) => item.frameworkDataType)
    );
  },
  { deep: true }
);

watch(
  localSelectedReportingPeriods,
  (newValue) => {
    emit(
      'update:selectedReportingPeriods',
      newValue.map((item) => item.displayName)
    );
  },
  { deep: true }
);

onMounted(() => {
  void retrieveAvailableFilterOptions().then(() => {
    localSelectedCountries.value = availableCountries.value.filter((item) =>
      props.selectedCountryCodes.includes(item.countryCode)
    );
    localSelectedSectors.value = availableSectors.value.filter((item) =>
      props.selectedSectors.includes(item.displayName)
    );
    localSelectedFrameworks.value = availableFrameworks.value.filter((item) =>
      props.selectedFrameworks.includes(item.frameworkDataType)
    );
    localSelectedReportingPeriods.value = availableReportingPeriods.value.filter((item) =>
      props.selectedReportingPeriods.includes(item.displayName)
    );
  });
});

/**
 * Resets all the filters to their default values (i.e., deselects everything)
 */
function resetFilters(): void {
  localSelectedFrameworks.value = [];
  localSelectedCountries.value = [];
  localSelectedSectors.value = [];
  localSelectedReportingPeriods.value = [];

  emit('update:selectedCountryCodes', []);
  emit('update:selectedSectors', []);
  emit('update:selectedFrameworks', []);
  emit('update:selectedReportingPeriods', []);
}

/**
 * A helper function that closes all the dropdown filters
 */
function closeAllOpenDropDowns(): void {
  countryFilter.value?.multiselect?.hide?.();

  sectorFilter.value?.multiselect?.hide?.();

  frameworkFilter.value?.multiselect?.hide?.();

  reportingPeriodFilter.value?.multiselect?.hide?.();
}

/**
 * Uses the Dataland API to obtain available company search filters and fills in the
 * availableCountries, availableSectors and availableReportingPeriods elements in the format
 * expected by the dropdown filters
 */
async function retrieveCountryAndSectorFilterOptions(): Promise<void> {
  const companyDataControllerApi = apiClientProvider.backendClients.companyDataController;

  const availableSearchFilters = await companyDataControllerApi.getAvailableCompanySearchFilters();
  availableCountries.value = [...(availableSearchFilters.data.countryCodes ?? [])]
    .map((countryCode) => {
      return {
        countryCode: countryCode,
        displayName: getCountryNameFromCountryCode(countryCode) ?? countryCode,
        disabled: false,
      };
    })
    .sort((a, b) => a.displayName.localeCompare(b.displayName));

  availableSectors.value = [...(availableSearchFilters.data.sectors ?? [])]
    .map((sector) => {
      return { displayName: sector, disabled: false };
    })
    .sort((a, b) => a.displayName.localeCompare(b.displayName));

  availableReportingPeriods.value = [...(availableSearchFilters.data.reportingPeriods ?? [])]
    .map((reportingPeriod) => {
      return { displayName: reportingPeriod, disabled: false };
    })
    .sort((a, b) => b.displayName.localeCompare(a.displayName));
}

/**
 * Populates the availableFrameworks property in the format expected by the dropdown filter
 */
function retrieveAvailableFrameworks(): void {
  availableFrameworks.value = FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
    let displayName = humanizeStringOrNumber(dataTypeEnum);
    const frameworkDefinition = getFrontendFrameworkDefinition(dataTypeEnum);
    if (frameworkDefinition) {
      displayName = frameworkDefinition.label;
    }

    return {
      frameworkDataType: dataTypeEnum,
      displayName: displayName,
      disabled: false,
    };
  });
}

/**
 * Initializes the availableCountries, availableSectors, availableReportingPeriods and available Frameworks
 * properties for the dropdown filters
 * @returns a promise as this function needs to request the Dataland api
 */
async function retrieveAvailableFilterOptions(): Promise<void> {
  retrieveAvailableFrameworks();
  return retrieveCountryAndSectorFilterOptions();
}

defineExpose({ closeAllOpenDropDowns });
</script>

<style scoped>
.filter-container {
  display: flex;
  flex-wrap: wrap;
  row-gap: var(--spacing-sm);
  min-height: 5rem;
  align-items: end;
  width: 100%;

  .filter {
    display: flex;
    flex-wrap: wrap;
    align-items: end;
    gap: var(--spacing-sm) var(--spacing-md);
    flex: 1 1 auto;
    min-width: 0;

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-xs);
      min-width: 0;

      label {
        text-align: left;
        font-size: var(--font-size-xs);
      }
    }

    .filter-group-items {
      display: flex;
      flex-wrap: wrap;
      gap: var(--spacing-sm);
    }

    .search-filter {
      width: 11rem;
      max-width: 100%;
      text-align: left;
    }

    .framework-filter {
      width: 20rem;
      max-width: 100%;
    }

    .filter-divider {
      align-self: stretch;
    }
  }
}

@media (max-width: 992px) {
  .filter-container {
    .filter {
      .filter-divider {
        display: none;
      }

      .search-filter,
      .framework-filter {
        width: 100%;
        flex: 1 1 9rem;
        min-width: 9rem;
      }
    }
  }
}

@media (max-width: 576px) {
  .filter-container {
    .filter {
      flex-direction: column;
      align-items: stretch;
      width: 100%;

      .filter-group-items {
        flex-direction: column;
      }

      .search-filter,
      .framework-filter {
        flex: 1 1 auto;
        width: 100%;
      }
    }
  }
}
</style>
