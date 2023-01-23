<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent class="pl-0 pt-0 min-h-screen paper-section relative">
      <div
        class="col-12 bg-white"
        :class="[searchBarToggled && pageScrolled ? ['d-search-toggle', 'fixed'] : '']"
        ref="searchBarAndFiltersContainer"
      >
        <div class="pt-4" />
        <MarginWrapper>
          <FrameworkDataSearchBar
            v-model="currentSearchBarInput"
            ref="frameworkDataSearchBar"
            :filter="currentCombinedFilter"
            :searchBarId="searchBarId"
            :emit-search-results-array="true"
            @search-confirmed="handleSearchConfirmed"
            @companies-received="handleCompanyQuery"
          />
          <div
            :class="[
              pageScrolled && !searchBarToggled
                ? ['grid', 'col-12', 'align-items-center', 'bg-white', 'd-search-toggle', 'fixed', 'd-shadow-bottom']
                : 'flex',
            ]"
          >
            <div :class="[pageScrolled && !searchBarToggled ? ['flex', 'align-items-center'] : 'hidden']">
              <span class="mr-3 font-semibold">Search Data for Companies</span>
              <PrimeButton
                name="search_bar_collapse"
                icon="pi pi-search"
                class="p-button-rounded surface-ground border-none m-2"
                @click="toggleSearchBar"
              >
                <i class="pi pi-search" aria-hidden="true" style="z-index: 20; color: #958d7c" />
              </PrimeButton>
            </div>
            <FrameworkDataSearchFilters
              class="ml-3"
              ref="frameworkDataSearchFilters"
              :show-heading="!pageScrolled || searchBarToggled"
              v-model:selected-country-codes="currentFilteredCountryCodes"
              v-model:selected-frameworks="currentFilteredFrameworks"
              v-model:selected-sectors="currentFilteredSectors"
            />
            <span v-show="!pageScrolled" class="d-page-display">{{ currentlyVisiblePageText }}</span>
          </div>
        </MarginWrapper>
      </div>
      <div v-if="waitingForSearchResults" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading...</p>
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <FrameworkDataSearchResults
        v-if="!waitingForSearchResults"
        ref="searchResults"
        :rows-per-page="rowsPerPage"
        :data="resultsArray"
        @update:first="setFirstShownRow"
      />
    </TheContent>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<style scoped>
.d-page-display {
  margin-left: auto;
  margin-top: auto;
  margin-bottom: 0;
  color: #5a4f36;
}
</style>

<script lang="ts">
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import TheContent from "@/components/generics/TheContent.vue";
import {
  FrameworkDataSearchFilterInterface,
  DataSearchStoredCompany,
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import FrameworkDataSearchBar from "@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue";
import PrimeButton from "primevue/button";
import FrameworkDataSearchResults from "@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue";
import { RouteLocationNormalizedLoaded, useRoute } from "vue-router";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import { defineComponent, ref } from "vue";
import { DataTypeEnum } from "@clients/backend";
import FrameworkDataSearchFilters from "@/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue";
import { parseQueryParamArray } from "@/utils/QueryParserUtils";
import { arraySetEquals } from "@/utils/ArrayUtils";
import { ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS } from "@/utils/Constants";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default defineComponent({
  name: "SearchCompaniesForFrameworkData",
  components: {
    FrameworkDataSearchFilters,
    MarginWrapper,
    AuthenticationWrapper,
    TheHeader,
    TheContent,
    FrameworkDataSearchBar,
    PrimeButton,
    FrameworkDataSearchResults,
    DatalandFooter,
  },

  created() {
    window.addEventListener("scroll", this.windowScrollHandler);
    this.scanQueryParams(this.route);
  },
  data() {
    return {
      searchBarToggled: false,
      pageScrolled: false,
      route: useRoute(),
      resultsArray: [] as Array<DataSearchStoredCompany>,
      latestScrollPosition: 0,
      currentSearchBarInput: "",
      currentFilteredFrameworks: ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS,
      currentFilteredCountryCodes: [] as Array<string>,
      currentFilteredSectors: [] as Array<string>,
      currentCombinedFilter: <FrameworkDataSearchFilterInterface>{
        companyNameFilter: "",
        frameworkFilter: [],
        sectorFilter: [],
        countryCodeFilter: [],
      },
      scrollEmittedByToggleSearchBar: false,
      hiddenSearchBarHeight: 0,
      searchBarId: "search_bar_top",
      indexOfFirstShownRow: 0,
      rowsPerPage: 100,
      waitingForSearchResults: true,
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
    };
  },
  beforeRouteUpdate(to: RouteLocationNormalizedLoaded) {
    this.scanQueryParams(to);
  },
  setup() {
    return {
      searchBarAndFiltersContainer: ref(),
      frameworkDataSearchBar: ref<typeof FrameworkDataSearchBar>(),
      frameworkDataSearchFilters: ref<typeof FrameworkDataSearchFilters>(),
      searchResults: ref(),
    };
  },
  watch: {
    currentFilteredFrameworks: {
      handler() {
        this.updateCombinedFilterIfRequired();
      },
      deep: true,
    },
    currentFilteredCountryCodes: {
      handler() {
        this.updateCombinedFilterIfRequired();
      },
      deep: true,
    },
    currentFilteredSectors: {
      handler() {
        this.updateCombinedFilterIfRequired();
      },
      deep: true,
    },
  },
  computed: {
    currentlyVisiblePageText(): string {
      const totalSearchResults = this.resultsArray.length;

      if (!this.waitingForSearchResults) {
        if (totalSearchResults === 0) {
          return "No results";
        } else {
          const startIndex = this.indexOfFirstShownRow;
          const endIndex =
            startIndex + (this.rowsPerPage - 1) >= totalSearchResults
              ? totalSearchResults - 1
              : startIndex + (this.rowsPerPage - 1);
          return `${startIndex + 1}-${endIndex + 1} of ${totalSearchResults} results`;
        }
      } else {
        return "loading...";
      }
    },
  },
  methods: {
    /**
     * Updates the local variable indicating which row of the datatable is currently displayed at the top
     *
     * @param value the index of the new row displayed on top
     */
    setFirstShownRow(value: number) {
      this.indexOfFirstShownRow = value;
    },
    /**
     * Called when the window is scrolled.
     * Handles the collapsing / uncollapsing of the search bar depending on the scroll position
     */
    handleScroll() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.frameworkDataSearchBar?.$refs.autocomplete.hide();
      const windowScrollY = window.scrollY;
      if (this.scrollEmittedByToggleSearchBar) {
        this.scrollEmittedByToggleSearchBar = false;
      } else {
        if (this.searchBarToggled) {
          this.searchBarToggled = false;
          this.searchBarId = "search_bar_top";
          window.scrollBy(0, this.hiddenSearchBarHeight);
        }
        if (this.latestScrollPosition > windowScrollY) {
          //ScrollUP event
          this.latestScrollPosition = windowScrollY;
          this.pageScrolled = document.documentElement.scrollTop >= 50;
          // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
          this.frameworkDataSearchFilters?.closeAllOpenDropDowns();
        } else {
          //ScrollDOWN event
          this.latestScrollPosition = windowScrollY;
          this.pageScrolled = document.documentElement.scrollTop > 100;
          // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
          this.frameworkDataSearchFilters?.closeAllOpenDropDowns();
        }
      }
    },
    /**
     * Parses the framework filter query parameters.
     *
     * @param route the current route
     * @returns an array of framework filters from the URL or an array of all frameworks if no filter is defined
     */
    getQueryFrameworks(route: RouteLocationNormalizedLoaded): Array<DataTypeEnum> {
      const queryFrameworks = route.query.framework;
      if (queryFrameworks !== undefined) {
        const allowedDataTypeEnumValues = ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS as Array<string>;
        return parseQueryParamArray(queryFrameworks).filter((it) =>
          allowedDataTypeEnumValues.includes(it)
        ) as Array<DataTypeEnum>;
      } else {
        return ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS;
      }
    },
    /**
     * Parses the country-code query parameters.
     *
     * @param route the current route
     * @returns an array of country codes to filter by or an empty array of no filter is present
     */
    getQueryCountryCodes(route: RouteLocationNormalizedLoaded): Array<string> {
      const queryCountryCodes = route.query.countryCode;
      if (queryCountryCodes) {
        return parseQueryParamArray(queryCountryCodes);
      }
      return [];
    },
    /**
     * Parses the sector-filter query parameters.
     *
     * @param route the current route
     * @returns an array of sectors to filter by or an empty array of no filter is present
     */
    getQuerySectors(route: RouteLocationNormalizedLoaded): Array<string> {
      const querySectors = route.query.sector;
      if (querySectors) {
        return parseQueryParamArray(querySectors);
      }
      return [];
    },
    /**
     * Parses the search term query parameter
     *
     * @param route the current route
     * @returns the parsed search term query parameter or an empty string if non-existent
     */
    getQueryInput(route: RouteLocationNormalizedLoaded): string {
      const queryInput = route.query.input as string;
      if (queryInput) {
        return queryInput;
      }
      return "";
    },
    /**
     * Updates the combined filter object if any of the local filters no longer match the combined filter object.
     * An update of the combined filter object automatically triggers a new search.
     */
    updateCombinedFilterIfRequired() {
      if (
        !arraySetEquals(this.currentFilteredFrameworks, this.currentCombinedFilter.frameworkFilter) ||
        !arraySetEquals(this.currentFilteredSectors, this.currentCombinedFilter.sectorFilter) ||
        !arraySetEquals(this.currentFilteredCountryCodes, this.currentCombinedFilter.countryCodeFilter) ||
        this.currentSearchBarInput !== this.currentCombinedFilter.companyNameFilter
      ) {
        this.waitingForSearchResults = true;
        this.currentCombinedFilter = {
          sectorFilter: this.currentFilteredSectors,
          frameworkFilter: this.currentFilteredFrameworks,
          companyNameFilter: this.currentSearchBarInput,
          countryCodeFilter: this.currentFilteredCountryCodes,
        };
      }
    },
    /**
     * Reads the query parameters of the framework-, country-code-, sector- and name- filters and
     * udpates the corresponding local variables accordingly
     *
     * @param route the current vue route
     */
    scanQueryParams(route: RouteLocationNormalizedLoaded) {
      const queryFrameworks = this.getQueryFrameworks(route);
      const queryCountryCodes = this.getQueryCountryCodes(route);
      const querySectors = this.getQuerySectors(route);
      const queryInput = this.getQueryInput(route);

      if (
        !arraySetEquals(this.currentFilteredFrameworks, queryFrameworks) ||
        !arraySetEquals(this.currentFilteredCountryCodes, queryCountryCodes) ||
        !arraySetEquals(this.currentFilteredSectors, querySectors) ||
        this.currentSearchBarInput !== queryInput
      ) {
        this.currentFilteredFrameworks = queryFrameworks;
        this.currentFilteredCountryCodes = queryCountryCodes;
        this.currentFilteredSectors = querySectors;
        this.currentSearchBarInput = queryInput;
      }
    },
    /**
     * Called when the new search results are received from the framework search bar. Disables the waiting indicator,
     * resets the pagination and updates the datatable. Also updates the query parameters to reflect the new search parameters
     *
     * @param companiesReceived the received companies
     * @returns the promise of the router push with the new query parameters
     */
    handleCompanyQuery(companiesReceived: Array<DataSearchStoredCompany>) {
      this.resultsArray = companiesReceived;
      this.setFirstShownRow(0);
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access
      this.searchResults?.resetPagination();
      this.waitingForSearchResults = false;
      this.searchBarToggled = false;

      const queryInput = this.currentSearchBarInput == "" ? undefined : this.currentSearchBarInput;

      const allFrameworksSelected = ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS.every((it) =>
        this.currentFilteredFrameworks.includes(it)
      );
      let queryFrameworks: DataTypeEnum[] | undefined | null = this.currentFilteredFrameworks;
      if (allFrameworksSelected) queryFrameworks = undefined;
      if (this.currentFilteredFrameworks.length == 0) queryFrameworks = null;

      const queryCountryCodes =
        this.currentFilteredCountryCodes.length == 0 ? undefined : this.currentFilteredCountryCodes;

      const querySectors = this.currentFilteredSectors.length == 0 ? undefined : this.currentFilteredSectors;
      return this.$router.push({
        name: "Search Companies for Framework Data",
        query: {
          input: queryInput,
          framework: queryFrameworks,
          countryCode: queryCountryCodes,
          sector: querySectors,
        },
      });
    },
    /**
     * Called when the user performed a company search. Updates the search bar contents and
     * displays the waiting indicator
     *
     * @param companyNameFilter the new search filter
     */
    handleSearchConfirmed(companyNameFilter: string) {
      this.waitingForSearchResults = true;
      this.currentSearchBarInput = companyNameFilter;
    },
    /**
     * A function that expands the searchbar that got collapsed when the user scrolled down
     */
    toggleSearchBar() {
      this.searchBarToggled = true;
      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
      const height = this.searchBarAndFiltersContainer?.clientHeight;
      window.scrollBy(0, -height);
      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
      this.hiddenSearchBarHeight = height;
      this.scrollEmittedByToggleSearchBar = true;
      this.searchBarId = "search_bar_scrolled";
    },
  },
  unmounted() {
    window.removeEventListener("scroll", this.windowScrollHandler);
  },
});
</script>
