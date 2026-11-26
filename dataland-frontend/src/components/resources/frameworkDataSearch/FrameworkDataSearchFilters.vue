<template>
  <div class="filter-container">
    <div class="filter">
      <div class="filter-group">
        <label for="sector-filter" v-if="showHeading">Filter by company</label>
        <div class="filter-group-items">
          <FrameworkDataSearchDropdownFilter
            v-model="selectedSectorItems"
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
            v-model="selectedCountryItems"
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
            v-model="selectedFrameworkItems"
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
            v-model="selectedReportingPeriodItems"
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
import { computed, ref, watch } from 'vue';
import { useGetAvailableCompanySearchFiltersQuery } from '@/api-queries/backend/company-data/useGetAvailableCompanySearchFiltersQuery';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { arraySetEquals } from '@/utils/ArrayUtils';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import {
  type CountryCodeSelectableItem,
  type FrameworkSelectableItem,
  type SelectableItem,
} from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
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

const { data: availableSearchFilters } = useGetAvailableCompanySearchFiltersQuery();

const availableFrameworks: Array<FrameworkSelectableItem> = FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => ({
  frameworkDataType: dataTypeEnum,
  displayName: getFrontendFrameworkDefinition(dataTypeEnum)?.label ?? humanizeStringOrNumber(dataTypeEnum),
  disabled: false,
}));

const availableCountries = computed<Array<CountryCodeSelectableItem>>(() =>
  [...(availableSearchFilters.value?.countryCodes ?? [])]
    .map((countryCode) => ({
      countryCode: countryCode,
      displayName: getCountryNameFromCountryCode(countryCode) ?? countryCode,
      disabled: false,
    }))
    .sort((a, b) => a.displayName.localeCompare(b.displayName))
);

const availableSectors = computed<Array<SelectableItem>>(() =>
  [...(availableSearchFilters.value?.sectors ?? [])]
    .map((sector) => ({ displayName: sector, disabled: false }))
    .sort((a, b) => a.displayName.localeCompare(b.displayName))
);

const availableReportingPeriods = computed<Array<SelectableItem>>(() =>
  [...(availableSearchFilters.value?.reportingPeriods ?? [])]
    .map((reportingPeriod) => ({ displayName: reportingPeriod, disabled: false }))
    .sort((a, b) => b.displayName.localeCompare(a.displayName))
);

const selectedCountryItems = computed({
  get: () => availableCountries.value.filter((item) => props.selectedCountryCodes.includes(item.countryCode)),
  set: (items: Array<CountryCodeSelectableItem>) =>
    emit(
      'update:selectedCountryCodes',
      items.map((item) => item.countryCode)
    ),
});

const selectedSectorItems = computed({
  get: () => availableSectors.value.filter((item) => props.selectedSectors.includes(item.displayName)),
  set: (items: Array<SelectableItem>) =>
    emit(
      'update:selectedSectors',
      items.map((item) => item.displayName)
    ),
});

const selectedFrameworkItems = computed({
  get: () => availableFrameworks.filter((item) => props.selectedFrameworks.includes(item.frameworkDataType)),
  set: (items: Array<FrameworkSelectableItem>) =>
    emit(
      'update:selectedFrameworks',
      items.map((item) => item.frameworkDataType)
    ),
});

const selectedReportingPeriodItems = computed({
  get: () =>
    availableReportingPeriods.value.filter((item) => props.selectedReportingPeriods.includes(item.displayName)),
  set: (items: Array<SelectableItem>) =>
    emit(
      'update:selectedReportingPeriods',
      items.map((item) => item.displayName)
    ),
});

/**
 * Once the filter options are loaded, drops selected values (e.g. from the URL) that are not available as options,
 * so that the applied filters match the selection shown in the dropdowns.
 */
watch(
  availableSearchFilters,
  (loadedSearchFilters) => {
    if (!loadedSearchFilters) return;
    const availableCountryCodes = selectedCountryItems.value.map((item) => item.countryCode);
    if (!arraySetEquals(availableCountryCodes, props.selectedCountryCodes)) {
      emit('update:selectedCountryCodes', availableCountryCodes);
    }
    const availableSelectedSectors = selectedSectorItems.value.map((item) => item.displayName);
    if (!arraySetEquals(availableSelectedSectors, props.selectedSectors)) {
      emit('update:selectedSectors', availableSelectedSectors);
    }
    const availableSelectedReportingPeriods = selectedReportingPeriodItems.value.map((item) => item.displayName);
    if (!arraySetEquals(availableSelectedReportingPeriods, props.selectedReportingPeriods)) {
      emit('update:selectedReportingPeriods', availableSelectedReportingPeriods);
    }
  },
  { immediate: true }
);

/**
 * Resets all the filters to their default values (i.e., deselects everything)
 */
function resetFilters(): void {
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
