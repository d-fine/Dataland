<template>
  <TheContent class="min-h-screen relative">
    <div
      id="searchBarAndFiltersContainer"
      class="search-bar-and-filters-container"
      :class="{ 'collapsed-search-container': isSearchBarContainerCollapsed }"
    >
      <FrameworkDataSearchBar
        id="frameworkDataSearchBar"
        ref="frameworkDataSearchBar"
        v-model="currentSearchBarInput"
        :filter="currentCombinedFilter"
        :chunk-size="rowsPerPage"
        :current-page="currentPage"
        :emit-search-results-array="true"
        @search-confirmed="handleSearchConfirmed"
        @companies-received="handleCompanyQuery"
      />

      <div class="search-filters-panel">
        <div>
          <FrameworkDataSearchFilters
            id="frameworkDataSearchFilters"
            class="col-8"
            ref="frameworkDataSearchFilters"
            v-model:selected-country-codes="currentFilteredCountryCodes"
            v-model:selected-frameworks="currentFilteredFrameworks"
            v-model:selected-sectors="currentFilteredSectors"
          />
        </div>

        <div v-if="!isSearchBarContainerCollapsed" class="button-container">
          <PrimeButton
            v-if="hasUserUploaderRights"
            icon="pi pi-plus"
            label="NEW DATASET"
            @click="linkToNewDatasetPage()"
            data-test="newDatasetButton"
          />
          <span>{{ currentlyVisiblePageText }}</span>
        </div>
      </div>
    </div>

    <div v-if="waitingForDataToDisplay" class="d-center-div text-center px-7 py-4">
      <p class="font-medium text-xl">Loading...</p>
      <DatalandProgressSpinner />
    </div>

    <FrameworkDataSearchResults
      v-if="!waitingForDataToDisplay"
      ref="searchResults"
      :total-records="totalRecords"
      :previous-records="previousRecords"
      :rows-per-page="rowsPerPage"
      :data="resultsArray"
      @page-update="handlePageUpdate"
    />
  </TheContent>
</template>

<script lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkDataSearchBar from '@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue';
import FrameworkDataSearchFilters from '@/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue';
import FrameworkDataSearchResults from '@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue';
import router from '@/router';
import { arraySetEquals } from '@/utils/ArrayUtils';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { parseQueryParamArray } from '@/utils/QueryParserUtils';
import { type FrameworkDataSearchFilterInterface } from '@/utils/SearchCompaniesForFrameworkDataPageDataRequester';
import { type BasicCompanyInformation, type DataTypeEnum } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { defineComponent, inject, ref } from 'vue';
import { type RouteLocationNormalizedLoaded, useRoute } from 'vue-router';

export default defineComponent({
  setup() {
    return {
      frameworkDataSearchFilters: ref<typeof FrameworkDataSearchFilters>(),
      frameworkDataSearchBar: ref<typeof FrameworkDataSearchBar>(),
      searchResults: ref(),
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'SearchCompaniesForFrameworkData',
  components: {
    DatalandProgressSpinner,
    FrameworkDataSearchFilters,
    TheContent,
    FrameworkDataSearchBar,
    FrameworkDataSearchResults,
    PrimeButton,
  },
  created() {
    globalThis.addEventListener('scroll', () => this.handleScroll());
    checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
    this.scanQueryParams(this.route);
    this.handleScroll();
  },
  data() {
    return {
      route: useRoute(),
      resultsArray: [] as Array<BasicCompanyInformation>,
      currentSearchBarInput: '',
      currentFilteredFrameworks: [] as Array<DataTypeEnum>,
      currentFilteredCountryCodes: [] as Array<string>,
      currentFilteredSectors: [] as Array<string>,
      currentCombinedFilter: {
        companyNameFilter: '',
        frameworkFilter: [],
        sectorFilter: [],
        countryCodeFilter: [],
      } as FrameworkDataSearchFilterInterface,
      isSearchBarContainerCollapsed: false,
      rowsPerPage: 100,
      currentPage: 0,
      totalRecords: 0,
      previousRecords: 0,
      waitingForDataToDisplay: true,
      hasUserUploaderRights: null as null | boolean,
    };
  },
  beforeRouteUpdate(to: RouteLocationNormalizedLoaded) {
    this.scanQueryParams(to);
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
      const totalSearchResults = this.totalRecords;
      if (this.waitingForDataToDisplay) {
        return 'loading...';
      } else if (totalSearchResults === 0) {
        return 'No results';
      } else {
        const startIndex = this.currentPage * this.rowsPerPage;
        const endIndex =
          startIndex + (this.rowsPerPage - 1) >= totalSearchResults
            ? totalSearchResults - 1
            : startIndex + (this.rowsPerPage - 1);
        return `${startIndex + 1}-${endIndex + 1} of ${totalSearchResults} results`;
      }
    },
  },
  methods: {
    /**
     * Redirects to the new dataset page
     */
    linkToNewDatasetPage() {
      void router.push('/companies/choose');
    },
    /**
     * Updates the current page.
     * An update of the currentPage automatically triggers a data Update
     * @param pageNumber the new page index
     */
    handlePageUpdate(pageNumber: number) {
      if (pageNumber !== this.currentPage) {
        this.waitingForDataToDisplay = true;
        this.currentPage = pageNumber;
        this.previousRecords = this.currentPage * this.rowsPerPage;
      }
    },
    /**
     * Called when the window is scrolled.
     * Handles the collapsing / uncollapsing of the search bar depending on the scroll position
     */
    handleScroll() {
      const y = window.scrollY || document.documentElement.scrollTop;
      const collapseAt = 160;
      const expandAt = 20;
      const shouldCollapse = this.isSearchBarContainerCollapsed ? y > expandAt : y >= collapseAt;

      if (shouldCollapse !== this.isSearchBarContainerCollapsed) {
        this.isSearchBarContainerCollapsed = shouldCollapse;
        this.frameworkDataSearchBar?.closeOverlay();
      }

      this.frameworkDataSearchFilters?.closeAllOpenDropDowns();
    },
    /**
     * Parses the framework filter query parameters.
     * @param route the current route
     * @returns an array of framework filters from the URL or an empty array if no filter is defined
     */
    getQueryFrameworks(route: RouteLocationNormalizedLoaded): Array<DataTypeEnum> {
      const queryFrameworks = route.query.framework;
      if (queryFrameworks) {
        const allowedDataTypeEnumValues = FRAMEWORKS_WITH_VIEW_PAGE as Array<string>;
        return parseQueryParamArray(queryFrameworks).filter((singleFrameworkInQueryParam) =>
          allowedDataTypeEnumValues.includes(singleFrameworkInQueryParam)
        ) as Array<DataTypeEnum>;
      } else {
        return [];
      }
    },
    /**
     * Parses the country-code query parameters.
     * @param route the current route
     * @returns an array of country codes to filter by or an empty array of no filter is present
     */
    getQueryCountryCodes(route: RouteLocationNormalizedLoaded): Array<string> {
      const queryCountryCodes = route.query.countryCode;
      return queryCountryCodes ? parseQueryParamArray(queryCountryCodes) : [];
    },
    /**
     * Parses the sector-filter query parameters.
     * @param route the current route
     * @returns an array of sectors to filter by or an empty array of no filter is present
     */
    getQuerySectors(route: RouteLocationNormalizedLoaded): Array<string> {
      const querySectors = route.query.sector;
      return querySectors ? parseQueryParamArray(querySectors) : [];
    },
    /**
     * Parses the search term query parameter
     * @param route the current route
     * @returns the parsed search term query parameter or an empty string if non-existent
     */
    getQueryInput(route: RouteLocationNormalizedLoaded): string {
      const queryInput = route.query.input as string;
      return queryInput || '';
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
        this.waitingForDataToDisplay = true;
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
     * @param companiesReceived the received chunk of companies
     * @param chunkIndex the index of the chunk
     * @param totalNumberOfCompanies the total number of companies
     * @returns the promise of the router push with the new query parameters
     */
    handleCompanyQuery(
      companiesReceived: Array<BasicCompanyInformation>,
      chunkIndex: number,
      totalNumberOfCompanies: number
    ) {
      this.totalRecords = totalNumberOfCompanies;
      this.resultsArray = companiesReceived;

      if (chunkIndex == 0) this.handlePageUpdate(0);
      this.waitingForDataToDisplay = false;

      const queryInput = this.currentSearchBarInput == '' ? undefined : this.currentSearchBarInput;
      const queryFrameworks = this.currentFilteredFrameworks.length == 0 ? undefined : this.currentFilteredFrameworks;
      const queryCountryCodes =
        this.currentFilteredCountryCodes.length == 0 ? undefined : this.currentFilteredCountryCodes;
      const querySectors = this.currentFilteredSectors.length == 0 ? undefined : this.currentFilteredSectors;

      return router.push({
        name: 'Search Companies for Framework Data',
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
     * @param companyNameFilter the new search filter
     */
    handleSearchConfirmed(companyNameFilter: string) {
      this.waitingForDataToDisplay = true;
      this.currentSearchBarInput = companyNameFilter;
    },
  },
  unmounted() {
    globalThis.removeEventListener('scroll', () => this.handleScroll());
  },
});
</script>

<style scoped>
.search-filters-panel {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}

.search-bar-and-filters-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: stretch;
  margin: 0;
  width: 100%;
  padding: var(--spacing-lg) 0 var(--spacing-xs) var(--spacing-lg);
  background-color: var(--p-surface-0);
  position: sticky;
  top: var(--spacing-xxxl);
  z-index: 10;
  contain: paint;
  will-change: padding-top;
}

.search-bar-and-filters-container #frameworkDataSearchBar {
  width: 70%;
}

.collapsed-search-container {
  flex-direction: row;
  align-items: end;
  padding-top: 0;
  border-bottom: 1px solid var(--p-surface-200);
}

.button-container {
  padding: 0 var(--spacing-xs);
  display: flex;
  flex-direction: row;
  gap: var(--spacing-md);
  align-items: center;
}
</style>
