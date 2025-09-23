<template>
  <TheContent class="min-h-screen relative">
    <div id="searchBarAndFiltersContainer" class="search-bar-and-filters-container">
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
            label="BULK DATA REQUEST"
            data-test="bulkDataRequestButton"
            @click="routeToBulkDataRequest()"
            icon="pi pi-file"
          />
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
import { type RouteLocationNormalizedLoaded } from 'vue-router';

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
  /**
   * Lifecycle hook that attaches the scroll listener, resolves user roles, and
   * initializes query param-driven state and collapsed state on first paint.
   * @returns {void}
   */
  mounted() {
    window.addEventListener('scroll', this.windowScrollHandler, { passive: true });
    checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, this.getKeycloakPromise)
      .then((hasUserUploaderRights) => {
        this.hasUserUploaderRights = hasUserUploaderRights;
      })
      .catch((error) => console.log(error));
    this.scanQueryParams(this.$route);
    this.handleScroll();
  },
  data() {
    return {
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
      shouldResetPagination: false,
      rowsPerPage: 100,
      currentPage: 0,
      totalRecords: 0,
      previousRecords: 0,
      waitingForDataToDisplay: true,
      /**
       * RequestAnimationFrame throttle flag to avoid handling multiple scroll events per frame.
       */
      isTicking: false,
      /**
       * Bound scroll handler reference for add/removeEventListener.
       * @returns {void}
       */
      windowScrollHandler: (): void => {
        this.handleScroll();
      },
      hasUserUploaderRights: null as null | boolean,
    };
  },
  /**
   * Updates component state when route changes (query params).
   * @param {RouteLocationNormalizedLoaded} to Next route location.
   * @returns {void}
   */
  beforeRouteUpdate(to: RouteLocationNormalizedLoaded) {
    this.scanQueryParams(to);
  },
  watch: {
    currentFilteredFrameworks: {
      /**
       * Triggers combined filter recomputation when frameworks change.
       * @returns {void}
       */
      handler() {
        this.updateCombinedFilterIfRequired();
      },
      deep: true,
    },
    currentFilteredCountryCodes: {
      /**
       * Triggers combined filter recomputation when country codes change.
       * @returns {void}
       */
      handler() {
        this.updateCombinedFilterIfRequired();
      },
      deep: true,
    },
    currentFilteredSectors: {
      /**
       * Triggers combined filter recomputation when sectors change.
       * @returns {void}
       */
      handler() {
        this.updateCombinedFilterIfRequired();
      },
      deep: true,
    },
  },
  computed: {
    /**
     * Human-readable string that indicates the currently visible slice of results.
     * @returns {string}
     */
    currentlyVisiblePageText(): string {
      const totalSearchResults = this.totalRecords;
      if (!this.waitingForDataToDisplay) {
        if (totalSearchResults === 0) {
          return 'No results';
        } else {
          const startIndex = this.currentPage * this.rowsPerPage;
          const endIndex =
            startIndex + (this.rowsPerPage - 1) >= totalSearchResults
              ? totalSearchResults - 1
              : startIndex + (this.rowsPerPage - 1);
          return `${startIndex + 1}-${endIndex + 1} of ${totalSearchResults} results`;
        }
      } else {
        return 'loading...';
      }
    },
  },
  methods: {
    /**
     * Navigates to the bulk data request page.
     * @returns {void}
     */
    routeToBulkDataRequest() {
      void router.push('/bulkdatarequest');
    },
    /**
     * Navigates to the new dataset page.
     * @returns {void}
     */
    linkToNewDatasetPage() {
      void router.push('/companies/choose');
    },
    /**
     * Updates the pagination state when the page changes.
     * @param {number} pageNumber Zero-based page index to display.
     * @returns {void}
     */
    handlePageUpdate(pageNumber: number) {
      if (pageNumber !== this.currentPage) {
        this.waitingForDataToDisplay = true;
        this.currentPage = pageNumber;
        this.previousRecords = this.currentPage * this.rowsPerPage;
      }
    },
    /**
     * Handles window scroll with hysteresis and rAF throttling.
     * Collapses the header after a threshold and expands below another threshold.
     * @returns {void}
     */
    handleScroll() {
      if (this.isTicking) return;
      this.isTicking = true;

      requestAnimationFrame(() => {
        const y = window.scrollY || document.documentElement.scrollTop;
        const collapseAt = 120;
        const expandAt = 32;
        const shouldCollapse = this.isSearchBarContainerCollapsed ? y > expandAt : y >= collapseAt;

        if (shouldCollapse !== this.isSearchBarContainerCollapsed) {
          this.isSearchBarContainerCollapsed = shouldCollapse;
          document
            .getElementById('searchBarAndFiltersContainer')
            ?.classList.toggle('collapsed-search-container', shouldCollapse);

          this.frameworkDataSearchFilters?.closeAllOpenDropDowns();
          this.frameworkDataSearchBar?.closeOverlay();
        }

        this.isTicking = false;
      });
    },
    /**
     * Parses framework filters from route query.
     * @param {RouteLocationNormalizedLoaded} route Current route.
     * @returns {Array<DataTypeEnum>} Allowed framework values.
     */
    getQueryFrameworks(route: RouteLocationNormalizedLoaded): Array<DataTypeEnum> {
      const queryFrameworks = route.query.framework;
      if (queryFrameworks) {
        const allowed = FRAMEWORKS_WITH_VIEW_PAGE as Array<string>;
        return parseQueryParamArray(queryFrameworks).filter((f) => allowed.includes(f)) as Array<DataTypeEnum>;
      } else {
        return [];
      }
    },
    /**
     * Parses country codes from route query.
     * @param {RouteLocationNormalizedLoaded} route Current route.
     * @returns {Array<string>} Country codes.
     */
    getQueryCountryCodes(route: RouteLocationNormalizedLoaded): Array<string> {
      const queryCountryCodes = route.query.countryCode;
      return queryCountryCodes ? parseQueryParamArray(queryCountryCodes) : [];
    },
    /**
     * Parses sectors from route query.
     * @param {RouteLocationNormalizedLoaded} route Current route.
     * @returns {Array<string>} Sector values.
     */
    getQuerySectors(route: RouteLocationNormalizedLoaded): Array<string> {
      const querySectors = route.query.sector;
      return querySectors ? parseQueryParamArray(querySectors) : [];
    },
    /**
     * Parses the search input from route query.
     * @param {RouteLocationNormalizedLoaded} route Current route.
     * @returns {string} Search input or empty string.
     */
    getQueryInput(route: RouteLocationNormalizedLoaded): string {
      const queryInput = route.query.input as string;
      return queryInput || '';
    },
    /**
     * Rebuilds the combined filter when any constituent filter changes.
     * Triggers a new search via v-model binding.
     * @returns {void}
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
        this.shouldResetPagination = true;
      }
    },
    /**
     * Reads query params and synchronizes the local filter state.
     * @param {RouteLocationNormalizedLoaded} route Route to read params from.
     * @returns {void}
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
        this.shouldResetPagination = true;
      }
    },
    /**
     * Receives result chunks, updates the data table and URL (first chunk only),
     * and conditionally resets pagination for a genuine new search.
     * @param {Array<BasicCompanyInformation>} companiesReceived Current chunk of companies.
     * @param {number} chunkIndex Zero-based chunk index.
     * @param {number} totalNumberOfCompanies Total matching records.
     * @returns {Promise<void | import('vue-router').NavigationFailure | undefined>}
     */
    handleCompanyQuery(
      companiesReceived: Array<BasicCompanyInformation>,
      chunkIndex: number,
      totalNumberOfCompanies: number
    ) {
      this.totalRecords = totalNumberOfCompanies;
      this.resultsArray = companiesReceived;

      if (this.shouldResetPagination && chunkIndex === 0) {
        if (this.currentPage !== 0) this.handlePageUpdate(0);
        this.shouldResetPagination = false;
      }
      this.waitingForDataToDisplay = false;

      if (chunkIndex === 0) {
        const queryInput = this.currentSearchBarInput === '' ? undefined : this.currentSearchBarInput;
        const queryFrameworks =
          this.currentFilteredFrameworks.length === 0 ? undefined : this.currentFilteredFrameworks;
        const queryCountryCodes =
          this.currentFilteredCountryCodes.length === 0 ? undefined : this.currentFilteredCountryCodes;
        const querySectors = this.currentFilteredSectors.length === 0 ? undefined : this.currentFilteredSectors;

        return router.replace({
          name: 'Search Companies for Framework Data',
          query: {
            input: queryInput,
            framework: queryFrameworks,
            countryCode: queryCountryCodes,
            sector: querySectors,
          },
        });
      }

      return Promise.resolve();
    },
    /**
     * Handles explicit search confirmations from the search bar.
     * @param {string} companyNameFilter New company name filter.
     * @returns {void}
     */
    handleSearchConfirmed(companyNameFilter: string) {
      this.waitingForDataToDisplay = true;
      this.currentSearchBarInput = companyNameFilter;
      this.shouldResetPagination = true;
    },
  },
  /**
   * Lifecycle hook that detaches the scroll listener.
   * @returns {void}
   */
  beforeUnmount() {
    window.removeEventListener('scroll', this.windowScrollHandler);
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

/* Match prod: expanded state with padding; sticky below app header */
.search-bar-and-filters-container {
  margin: 0;
  width: 100%;
  padding-left: var(--spacing-lg);
  padding-top: var(--spacing-lg);
  padding-bottom: var(--spacing-xs);
  background-color: var(--p-surface-0);
  position: sticky;
  top: var(--app-header-offset, 4rem);
  z-index: 50;
}
.search-bar-and-filters-container #frameworkDataSearchBar {
  width: 70%;
}

/* Collapsed: reduce top padding and align horizontally */
.collapsed-search-container {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: end;
  padding-top: 0;
  padding-bottom: var(--spacing-xs);
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
