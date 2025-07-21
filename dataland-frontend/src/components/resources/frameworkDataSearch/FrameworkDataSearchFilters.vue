<template>
  <div class="filter-container">
    <div class="filter">
      <label for="sector-filter" v-if="showHeading">Filter by company</label>
      <FrameworkDataSearchDropdownFilter
        v-model="selectedSectorsInt"
        ref="sectorFilter"
        :available-items="availableSectors"
        filter-name="Sector"
        id="sector-filter"
        filter-placeholder="Search sectors"
        max-selected-labels="1"
        selected-items-label="{0} sectors"
        class="search-filter"
        data-test="frameworkDataSearchDropdownFilterSector"
      />
      <FrameworkDataSearchDropdownFilter
        v-model="selectedCountriesInt"
        ref="countryFilter"
        :available-items="availableCountries"
        filter-name="Country"
        id="country-filter"
        filter-placeholder="Search countries"
        max-selected-labels="1"
        selected-items-label="{0} companies"
        class="search-filter"
      />
      <Divider layout="vertical" />
      <FrameworkDataSearchDropdownFilter
        v-model="selectedFrameworksInt"
        ref="frameworkFilter"
        :available-items="availableFrameworks"
        filter-name="Framework"
        id="framework-filter"
        filter-placeholder="Search frameworks"
        selected-items-label="{0} frameworks"
        max-selected-labels="1"
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
import { type DataTypeEnum } from '@clients/backend';
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
      availableCountries: [] as Array<CountryCodeSelectableItem>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
      availableSectors: [] as Array<SelectableItem>,
    };
  },
  computed: {
    selectedCountriesInt: {
      get(): Array<CountryCodeSelectableItem> {
        return this.availableCountries.filter((countryCodeSelectableItem) =>
          this.selectedCountryCodes.includes(countryCodeSelectableItem.countryCode)
        );
      },
      set(newValue: Array<CountryCodeSelectableItem>) {
        this.$emit(
          'update:selectedCountryCodes',
          newValue.map((countryCodeSelectableItem) => countryCodeSelectableItem.countryCode)
        );
      },
    },
    selectedFrameworksInt: {
      get(): Array<FrameworkSelectableItem> {
        return this.availableFrameworks.filter((frameworkSelectableItem) =>
          this.selectedFrameworks.includes(frameworkSelectableItem.frameworkDataType)
        );
      },
      set(newValue: Array<FrameworkSelectableItem>) {
        this.$emit(
          'update:selectedFrameworks',
          newValue.map((frameworkSelectableItem) => frameworkSelectableItem.frameworkDataType)
        );
      },
    },
    selectedSectorsInt: {
      get(): Array<SelectableItem> {
        return this.availableSectors.filter((selectableItem) =>
          this.selectedSectors.includes(selectableItem.displayName)
        );
      },
      set(newValue: Array<SelectableItem>) {
        this.$emit(
          'update:selectedSectors',
          newValue.map((selectableItem) => selectableItem.displayName)
        );
      },
    },
  },
  methods: {
    /**
     * Resets all the filters to their default values (i.e., deselects everything)
     */
    resetFilters() {
      this.selectedFrameworksInt = [];
      this.selectedCountriesInt = [];
      this.selectedSectorsInt = [];
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
    void this.retrieveAvailableFilterOptions();
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
