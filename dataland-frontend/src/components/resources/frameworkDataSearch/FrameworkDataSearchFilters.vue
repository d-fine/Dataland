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

<script lang="ts">
import { defineComponent, inject, ref } from 'vue';
import { type ApiClientProvider } from '@/services/ApiClients';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter';
import FrameworkDataSearchDropdownFilter from '@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
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

export default defineComponent({
  name: 'FrameworkDataSearchFilters',
  components: { FrameworkDataSearchDropdownFilter, PrimeButton, Divider },
  emits: [
    'update:selectedCountryCodes',
    'update:selectedFrameworks',
    'update:selectedSectors',
    'update:selectedReportingPeriods',
  ],
  setup() {
    return {
      sectorFilter: ref<FrameworkDataSearchDropdownFilterRef | null>(null),
      countryFilter: ref<FrameworkDataSearchDropdownFilterRef | null>(null),
      frameworkFilter: ref<FrameworkDataSearchDropdownFilterRef | null>(null),
      reportingPeriodFilter: ref<FrameworkDataSearchDropdownFilterRef | null>(null),
      apiClientProvider: inject<ApiClientProvider>('apiClientProvider'),
    };
  },
  props: {
    selectedFrameworks: {
      type: Array as () => Array<DataTypeEnum>,
      default: () => [],
    },
    selectedSectors: {
      type: Array as () => Array<string>,
      default: () => [],
    },
    selectedCountryCodes: {
      type: Array as () => Array<string>,
      default: () => [],
    },
    selectedReportingPeriods: {
      type: Array as () => Array<string>,
      default: () => [],
    },
    showHeading: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      localSelectedCountries: [] as Array<CountryCodeSelectableItem>,
      localSelectedFrameworks: [] as Array<FrameworkSelectableItem>,
      localSelectedSectors: [] as Array<SelectableItem>,
      localSelectedReportingPeriods: [] as Array<SelectableItem>,

      availableCountries: [] as Array<CountryCodeSelectableItem>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      availableSectors: [] as Array<SelectableItem>,
      availableReportingPeriods: [] as Array<SelectableItem>,
    };
  },
  watch: {
    localSelectedCountries: {
      deep: true,
      handler(newValue: Array<CountryCodeSelectableItem>) {
        this.$emit(
          'update:selectedCountryCodes',
          newValue.map((item) => item.countryCode)
        );
      },
    },
    localSelectedSectors: {
      deep: true,
      handler(newValue: Array<SelectableItem>) {
        this.$emit(
          'update:selectedSectors',
          newValue.map((item) => item.displayName)
        );
      },
    },
    localSelectedFrameworks: {
      deep: true,
      handler(newValue: Array<FrameworkSelectableItem>) {
        this.$emit(
          'update:selectedFrameworks',
          newValue.map((item) => item.frameworkDataType)
        );
      },
    },
    localSelectedReportingPeriods: {
      deep: true,
      handler(newValue: Array<SelectableItem>) {
        this.$emit(
          'update:selectedReportingPeriods',
          newValue.map((item) => item.displayName)
        );
      },
    },
  },
  methods: {
    /**
     * Resets all the filters to their default values (i.e., deselects everything)
     */
    resetFilters() {
      this.localSelectedFrameworks = [];
      this.localSelectedCountries = [];
      this.localSelectedSectors = [];
      this.localSelectedReportingPeriods = [];

      this.$emit('update:selectedCountryCodes', []);
      this.$emit('update:selectedSectors', []);
      this.$emit('update:selectedFrameworks', []);
      this.$emit('update:selectedReportingPeriods', []);
    },
    /**
     * A helper function that closes all the dropdown filters
     */
    // The following method is used, the linter reports a false positive here
    // eslint-disable-next-line vue/no-unused-properties
    closeAllOpenDropDowns() {
      this.countryFilter?.multiselect?.hide?.();

      this.sectorFilter?.multiselect?.hide?.();

      this.frameworkFilter?.multiselect?.hide?.();

      this.reportingPeriodFilter?.multiselect?.hide?.();
    },
    /**
     * Uses the Dataland API to obtain available company search filters and fills in the
     * availableCountries, availableSectors and availableReportingPeriods elements in the format
     * expected by the dropdown filters
     */
    async retrieveCountryAndSectorFilterOptions() {
      const companyDataControllerApi = assertDefined(this.apiClientProvider).backendClients.companyDataController;

      const availableSearchFilters = await companyDataControllerApi.getAvailableCompanySearchFilters();
      this.availableCountries = [...(availableSearchFilters.data.countryCodes ?? [])]
        .map((countryCode) => {
          return {
            countryCode: countryCode,
            displayName: getCountryNameFromCountryCode(countryCode) ?? countryCode,
            disabled: false,
          };
        })
        .sort((a, b) => a.displayName.localeCompare(b.displayName));

      this.availableSectors = [...(availableSearchFilters.data.sectors ?? [])]
        .map((sector) => {
          return { displayName: sector, disabled: false };
        })
        .sort((a, b) => a.displayName.localeCompare(b.displayName));

      this.availableReportingPeriods = [...(availableSearchFilters.data.reportingPeriods ?? [])]
        .map((reportingPeriod) => {
          return { displayName: reportingPeriod, disabled: false };
        })
        .sort((a, b) => b.displayName.localeCompare(a.displayName));
    },
    /**
     * Populates the availableFrameworks property in the format expected by the dropdown filter
     */
    retrieveAvailableFrameworks() {
      this.availableFrameworks = FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
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
    },
    /**
     * Initializes the availableCountries, availableSectors, availableReportingPeriods and available Frameworks
     * properties for the dropdown filters
     * @returns a promise as this function needs to request the Dataland api
     */
    async retrieveAvailableFilterOptions() {
      this.retrieveAvailableFrameworks();
      return this.retrieveCountryAndSectorFilterOptions();
    },
  },
  mounted() {
    void this.retrieveAvailableFilterOptions().then(() => {
      this.localSelectedCountries = this.availableCountries.filter((item) =>
        this.selectedCountryCodes.includes(item.countryCode)
      );
      this.localSelectedSectors = this.availableSectors.filter((item) =>
        this.selectedSectors.includes(item.displayName)
      );
      this.localSelectedFrameworks = this.availableFrameworks.filter((item) =>
        this.selectedFrameworks.includes(item.frameworkDataType)
      );
      this.localSelectedReportingPeriods = this.availableReportingPeriods.filter((item) =>
        this.selectedReportingPeriods.includes(item.displayName)
      );
    });
  },
});
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
