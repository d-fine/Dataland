<template>
  <div class="flex">
    <FrameworkDataSearchDropdownFilter
      v-model="selectedCountriesInt"
      :available-items="availableCountries"
      filter-name="Country"
    />
    <FrameworkDataSearchDropdownFilter
      v-model="selectedFrameworksInt"
      :available-items="availableFrameworks"
      filter-name="Framework"
      class="ml-2"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodes";
import FrameworkDataSearchDropdownFilter, {
  SelectableItem,
} from "@/components/resources/frameworkDataSearch/FrameworkDataSearchDropdownFilter.vue";
import { DataTypeEnum } from "@clients/backend";
import { humanizeString } from "@/utils/StringHumanizer";
import { useRoute } from "vue-router";

interface CountryCodeSelectableItem extends SelectableItem {
  countryCode: string;
}

interface FrameworkSelectableItem extends SelectableItem {
  frameworkDataType: DataTypeEnum;
}

export default defineComponent({
  name: "FrameworkDataSearchFilters",
  components: { FrameworkDataSearchDropdownFilter },
  emits: ["update:selectedCountryCodes", "update:selectedFrameworks"],
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  props: {
    selectedCountryCodes: {
      type: Array as () => Array<string>,
      default: () => [],
    },
    selectedFrameworks: {
      type: Array as () => Array<DataTypeEnum>,
      default: () => [],
    },
  },
  data() {
    return {
      route: useRoute(),
      availableCountries: [] as Array<CountryCodeSelectableItem>,
      availableFrameworks: [] as Array<FrameworkSelectableItem>,
    };
  },
  computed: {
    selectedCountriesInt: {
      get(): Array<CountryCodeSelectableItem> {
        return this.availableCountries.filter((it) => this.selectedCountryCodes.indexOf(it.countryCode) >= 0);
      },
      set(newValue: Array<CountryCodeSelectableItem>) {
        this.$emit(
          "update:selectedCountryCodes",
          newValue.map((it) => it.countryCode)
        );
      },
    },
    selectedFrameworksInt: {
      get(): Array<FrameworkSelectableItem> {
        return this.availableFrameworks.filter((it) => this.selectedFrameworks.indexOf(it.frameworkDataType) >= 0);
      },
      set(newValue: Array<FrameworkSelectableItem>) {
        this.$emit(
          "update:selectedFrameworks",
          newValue.map((it) => it.frameworkDataType)
        );
      },
    },
  },
  methods: {
    async retrieveCountryFilterOptions() {
      const companyDataControllerApi = await new ApiClientProvider(
        this.getKeycloakPromise!!()
      ).getCompanyDataControllerApi();
      const availableSearchFilters = await companyDataControllerApi.getAvailableCompanySearchFilters();
      this.availableCountries = [...availableSearchFilters.data.countryCodes!!].map((it) => {
        return {
          countryCode: it,
          displayName: getCountryNameFromCountryCode(it),
        };
      });
    },
    async retrieveAvailableFilterOptions() {
      this.availableFrameworks = Object.values(DataTypeEnum).map((it) => {
        return {
          frameworkDataType: it,
          displayName: humanizeString(it),
        };
      });
      await this.retrieveCountryFilterOptions();
    },
  },
  async mounted() {
    await this.retrieveAvailableFilterOptions();
  },
});
</script>
