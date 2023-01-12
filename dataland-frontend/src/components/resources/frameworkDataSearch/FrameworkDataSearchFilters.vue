<template>
  <div class="flex">
    <div class="flex flex-column">
      <span class="d-section-heading mb-2" v-if="showHeading">Filter by company</span>
      <div>
        <FrameworkDataSearchDropdownFilter
          v-model="selectedSectorsInt"
          ref="sectorFilter"
          :available-items="availableSectors"
          filter-name="Sector"
          filter-id="sector-filter"
          filter-placeholder="Search sectors"
        />
        <FrameworkDataSearchDropdownFilter
          v-model="selectedCountriesInt"
          ref="countryFilter"
          :available-items="availableCountries"
          filter-name="Country"
          filter-id="country-filter"
          filter-placeholder="Search countries"
          class="ml-3"
        />
      </div>
    </div>
    <div class="flex flex-column ml-3">
      <span class="d-section-heading mb-2" v-if="showHeading">Filter by data</span>
      <div class="flex flex-row align-items-center">
        <div class="d-separator-left" />
        <FrameworkDataSearchDropdownFilter
          v-model="selectedFrameworksInt"
          ref="frameworkFilter"
          :available-items="availableFrameworks"
          filter-name="Framework"
          filter-id="framework-filter"
          filter-placeholder="Search frameworks"
          class="ml-3"
        />
        <div class="d-separator-left ml-3" />
        <span class="ml-3 cursor-pointer text-primary font-semibold d-letters" @click="resetFilters">RESET</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.d-section-heading {
  text-align: left;
  font-size: 0.75rem;
  color: #5a4f36;
}

.d-separator-left {
  height: 2rem;
  border-left: 1px solid #5a4f36;
}
</style>

<script lang="ts">
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodes";
import FrameworkDataSearchDropdownFilter from "@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue";
import { DataTypeEnum } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import { useRoute } from "vue-router";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import {
  CountryCodeSelectableItem,
  FrameworkSelectableItem,
  SelectableItem,
} from "@/utils/FrameworkDataSearchDropDownFilterTypes";

export default defineComponent({
  name: "FrameworkDataSearchFilters",
  components: { FrameworkDataSearchDropdownFilter },
  emits: ["update:selectedCountryCodes", "update:selectedFrameworks", "update:selectedSectors"],
  setup() {
    return {
      sectorFilter: ref(),
      countryFilter: ref(),
      frameworkFilter: ref(),
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
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
      route: useRoute(),
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
          "update:selectedCountryCodes",
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
          "update:selectedFrameworks",
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
          "update:selectedSectors",
          newValue.map((selectableItem) => selectableItem.displayName)
        );
      },
    },
  },
  methods: {
    resetFilters() {
      this.selectedFrameworksInt = this.availableFrameworks.filter((it) => !it.disabled);
      this.selectedCountriesInt = [];
      this.selectedSectorsInt = [];
    },
    closeAllOpenDropDowns() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.countryFilter?.$refs.multiselect.hide();
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.sectorFilter?.$refs.multiselect.hide();
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.frameworkFilter?.$refs.multiselect.hide();
    },
    async retrieveCountryAndSectorFilterOptions() {
      const companyDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getCompanyDataControllerApi();

      const availableSearchFilters = await companyDataControllerApi.getAvailableCompanySearchFilters();
      this.availableCountries = [...(availableSearchFilters.data.countryCodes || [])].map((countryCode) => {
        return {
          countryCode: countryCode,
          displayName: getCountryNameFromCountryCode(countryCode),
          disabled: false,
        };
      });
      this.availableSectors = [...(availableSearchFilters.data.sectors || [])].map((sector) => {
        return { displayName: sector, disabled: false };
      });
    },
    retrieveAvailableFrameworks() {
      this.availableFrameworks = ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.map((dataTypeEnum) => {
        return {
          frameworkDataType: dataTypeEnum,
          displayName: humanizeString(dataTypeEnum),
          disabled: false,
        };
      });
      this.availableFrameworks.push({
        frameworkDataType: "sfdr" as DataTypeEnum,
        displayName: "SFDR",
        disabled: true,
      });
    },
    retrieveAvailableFilterOptions() {
      this.retrieveAvailableFrameworks();
      return this.retrieveCountryAndSectorFilterOptions();
    },
  },
  mounted() {
    void this.retrieveAvailableFilterOptions();
  },
});
</script>
