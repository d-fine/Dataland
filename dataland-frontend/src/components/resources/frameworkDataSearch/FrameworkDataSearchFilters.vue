<template>
  <div class="filter-container">
    <div class="filter">
      <label for="sector-filter" v-if="showHeading">Filter by company</label>
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
      <Divider layout="vertical" />
      <FrameworkDataSearchDropdownFilter
        v-model="localSelectedFrameworks"
        ref="frameworkFilter"
        :available-items="availableFrameworks"
        filter-name="Framework"
        id="framework-filter"
        filter-placeholder="Search frameworks"
        selected-items-label="{0} frameworks"
        :max-selected-labels="1"
        class="search-filter"
      />
      <label for="framework-filter" v-if="showHeading">Filter for available data sets</label>
      <Divider layout="vertical" />
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

export default defineComponent({
  name: 'FrameworkDataSearchFilters',
  components: { FrameworkDataSearchDropdownFilter, PrimeButton, Divider },
  emits: ['update:selectedCountryCodes', 'update:selectedFrameworks', 'update:selectedSectors'],
  setup() {
    return {
      sectorFilter: ref(),
      countryFilter: ref(),
      frameworkFilter: ref(),
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

      availableCountries: [] as Array<CountryCodeSelectableItem>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      availableSectors: [] as Array<SelectableItem>,
    };
  },
  watch: {
    localSelectedCountries: {
      deep: true,
      handler(newValue: Array<CountryCodeSelectableItem>) {
        console.log('Watcher: localSelectedCountries changed:', newValue);
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
  },
  methods: {
    /**
     * Resets all the filters to their default values (i.e., deselects everything)
     */
    resetFilters() {
      this.localSelectedFrameworks = [];
      this.localSelectedCountries = [];
      this.localSelectedSectors = [];

      this.$emit('update:selectedCountryCodes', []);
      this.$emit('update:selectedSectors', []);
      this.$emit('update:selectedFrameworks', []);
    },
    /**
     * A helper function that closes all the dropdown filters
     */
    // The following method is used, the linter reports a false positive here
    // eslint-disable-next-line vue/no-unused-properties
    closeAllOpenDropDowns() {
      this.countryFilter?.$refs.multiselect.hide();

      this.sectorFilter?.$refs.multiselect.hide();

      this.frameworkFilter?.$refs.multiselect.hide();
    },
    /**
     * Uses the Dataland API to obtain available company search filters and fills in the
     * availableCountries and availableSectors elements in the format expected by the dropdown filters
     */
    async retrieveCountryAndSectorFilterOptions() {
      const companyDataControllerApi = assertDefined(this.apiClientProvider).backendClients.companyDataController;

      const availableSearchFilters = await companyDataControllerApi.getAvailableCompanySearchFilters();
      this.availableCountries = [...(availableSearchFilters.data.countryCodes ?? [])]
        .map((countryCode) => {
          return {
            countryCode: countryCode,
            displayName: getCountryNameFromCountryCode(countryCode) as string,
            disabled: false,
          };
        })
        .sort((a, b) => a.displayName.localeCompare(b.displayName));

      this.availableSectors = [...(availableSearchFilters.data.sectors ?? [])]
        .map((sector) => {
          return { displayName: sector, disabled: false };
        })
        .sort((a, b) => a.displayName.localeCompare(b.displayName));
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
     * Initializes the availableCountries, availableSectors and available Frameworks properties for the dropdown filters
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
    });
  },
});
</script>

<style scoped>
.filter-container {
  display: flex;
  min-height: 5rem;
  align-items: end;
  width: 100%;

  .filter {
    display: grid;
    gap: 0 var(--spacing-sm);

    .search-filter {
      width: 11rem;
      text-align: left;
    }

    label {
      grid-row: 1;
      margin-bottom: var(--spacing-xs);
      text-align: left;
      font-size: var(--font-size-xs);

      &:last-of-type {
        grid-column-start: 4;
      }
    }

    :not(label) {
      grid-row: 2;
    }
  }
}
</style>
