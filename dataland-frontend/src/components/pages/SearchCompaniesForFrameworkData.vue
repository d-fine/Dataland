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
            v-model:selected-reporting-periods="currentFilteredReportingPeriods"
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

<script setup lang="ts">
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
import { computed, inject, onUnmounted, ref, watch } from 'vue';
import { type NavigationFailure, type RouteLocationNormalizedLoaded, onBeforeRouteUpdate, useRoute } from 'vue-router';

interface FrameworkDataSearchBarRef {
  closeOverlay?: () => void;
}

interface FrameworkDataSearchFiltersRef {
  closeAllOpenDropDowns?: () => void;
}

const frameworkDataSearchFilters = ref<FrameworkDataSearchFiltersRef | null>(null);
const frameworkDataSearchBar = ref<FrameworkDataSearchBarRef | null>(null);
const searchResults = ref<unknown>(null);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const route = useRoute();
const resultsArray = ref<Array<BasicCompanyInformation>>([]);
const currentSearchBarInput = ref('');
const currentFilteredFrameworks = ref<Array<DataTypeEnum>>([]);
const currentFilteredCountryCodes = ref<Array<string>>([]);
const currentFilteredSectors = ref<Array<string>>([]);
const currentFilteredReportingPeriods = ref<Array<string>>([]);
const currentCombinedFilter = ref<FrameworkDataSearchFilterInterface>({
  companyNameFilter: '',
  frameworkFilter: [],
  sectorFilter: [],
  countryCodeFilter: [],
  reportingPeriodFilter: [],
});
const isSearchBarContainerCollapsed = ref(false);
const rowsPerPage = ref(100);
const currentPage = ref(0);
const totalRecords = ref(0);
const previousRecords = ref(0);
const waitingForDataToDisplay = ref(true);
const hasUserUploaderRights = ref<boolean | null>(null);

const currentlyVisiblePageText = computed((): string => {
  const totalSearchResults = totalRecords.value;
  if (waitingForDataToDisplay.value) {
    return 'loading...';
  } else if (totalSearchResults === 0) {
    return 'No results';
  } else {
    const startIndex = currentPage.value * rowsPerPage.value;
    const endIndex =
      startIndex + (rowsPerPage.value - 1) >= totalSearchResults
        ? totalSearchResults - 1
        : startIndex + (rowsPerPage.value - 1);
    return `${startIndex + 1}-${endIndex + 1} of ${totalSearchResults} results`;
  }
});

watch(
  currentFilteredFrameworks,
  () => {
    updateCombinedFilterIfRequired();
  },
  { deep: true }
);

watch(
  currentFilteredCountryCodes,
  () => {
    updateCombinedFilterIfRequired();
  },
  { deep: true }
);

watch(
  currentFilteredSectors,
  () => {
    updateCombinedFilterIfRequired();
  },
  { deep: true }
);

watch(
  currentFilteredReportingPeriods,
  () => {
    updateCombinedFilterIfRequired();
  },
  { deep: true }
);

onBeforeRouteUpdate((to) => {
  scanQueryParams(to);
});

globalThis.addEventListener('scroll', handleScroll);
checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, getKeycloakPromise)
  .then((userHasUploaderRights) => {
    hasUserUploaderRights.value = userHasUploaderRights;
  })
  .catch((error) => console.log(error));
scanQueryParams(route);
handleScroll();

onUnmounted(() => {
  globalThis.removeEventListener('scroll', handleScroll);
});

/**
 * Redirects to the new dataset page
 */
function linkToNewDatasetPage(): void {
  void router.push('/companies/choose');
}

/**
 * Updates the current page.
 * An update of the currentPage automatically triggers a data Update
 * @param pageNumber the new page index
 */
function handlePageUpdate(pageNumber: number): void {
  if (pageNumber !== currentPage.value) {
    waitingForDataToDisplay.value = true;
    currentPage.value = pageNumber;
    previousRecords.value = currentPage.value * rowsPerPage.value;
  }
}

/**
 * Called when the window is scrolled.
 * Handles the collapsing / uncollapsing of the search bar depending on the scroll position
 */
function handleScroll(): void {
  const y = window.scrollY || document.documentElement.scrollTop;
  const collapseAt = 160;
  const expandAt = 20;
  const shouldCollapse = isSearchBarContainerCollapsed.value ? y > expandAt : y >= collapseAt;

  if (shouldCollapse !== isSearchBarContainerCollapsed.value) {
    isSearchBarContainerCollapsed.value = shouldCollapse;
    frameworkDataSearchBar.value?.closeOverlay?.();
  }

  frameworkDataSearchFilters.value?.closeAllOpenDropDowns?.();
}

/**
 * Parses the framework filter query parameters.
 * @param route the current route
 * @returns an array of framework filters from the URL or an empty array if no filter is defined
 */
function getQueryFrameworks(route: RouteLocationNormalizedLoaded): Array<DataTypeEnum> {
  const queryFrameworks = route.query.framework;
  if (queryFrameworks) {
    const allowedDataTypeEnumValues = FRAMEWORKS_WITH_VIEW_PAGE as Array<string>;
    return parseQueryParamArray(queryFrameworks).filter((singleFrameworkInQueryParam) =>
      allowedDataTypeEnumValues.includes(singleFrameworkInQueryParam)
    ) as Array<DataTypeEnum>;
  } else {
    return [];
  }
}

/**
 * Parses the country-code query parameters.
 * @param route the current route
 * @returns an array of country codes to filter by or an empty array of no filter is present
 */
function getQueryCountryCodes(route: RouteLocationNormalizedLoaded): Array<string> {
  const queryCountryCodes = route.query.countryCode;
  return queryCountryCodes ? parseQueryParamArray(queryCountryCodes) : [];
}

/**
 * Parses the sector-filter query parameters.
 * @param route the current route
 * @returns an array of sectors to filter by or an empty array of no filter is present
 */
function getQuerySectors(route: RouteLocationNormalizedLoaded): Array<string> {
  const querySectors = route.query.sector;
  return querySectors ? parseQueryParamArray(querySectors) : [];
}

/**
 * Parses the search term query parameter
 * @param route the current route
 * @returns the parsed search term query parameter or an empty string if non-existent
 */
function getQueryInput(route: RouteLocationNormalizedLoaded): string {
  const queryInput = route.query.input as string;
  return queryInput || '';
}

/**
 * Updates the combined filter object if any of the local filters no longer match the combined filter object.
 * An update of the combined filter object automatically triggers a new search.
 */
function updateCombinedFilterIfRequired(): void {
  if (
    !arraySetEquals(currentFilteredFrameworks.value, currentCombinedFilter.value.frameworkFilter) ||
    !arraySetEquals(currentFilteredSectors.value, currentCombinedFilter.value.sectorFilter) ||
    !arraySetEquals(currentFilteredCountryCodes.value, currentCombinedFilter.value.countryCodeFilter) ||
    !arraySetEquals(currentFilteredReportingPeriods.value, currentCombinedFilter.value.reportingPeriodFilter) ||
    currentSearchBarInput.value !== currentCombinedFilter.value.companyNameFilter
  ) {
    waitingForDataToDisplay.value = true;
    currentCombinedFilter.value = {
      sectorFilter: currentFilteredSectors.value,
      frameworkFilter: currentFilteredFrameworks.value,
      companyNameFilter: currentSearchBarInput.value,
      countryCodeFilter: currentFilteredCountryCodes.value,
      reportingPeriodFilter: currentFilteredReportingPeriods.value,
    };
  }
}

/**
 * Reads the query parameters of the framework-, country-code-, sector- and name- filters and
 * udpates the corresponding local variables accordingly
 * @param route the current vue route
 */
function scanQueryParams(route: RouteLocationNormalizedLoaded): void {
  const queryFrameworks = getQueryFrameworks(route);
  const queryCountryCodes = getQueryCountryCodes(route);
  const querySectors = getQuerySectors(route);
  const queryInput = getQueryInput(route);
  if (
    !arraySetEquals(currentFilteredFrameworks.value, queryFrameworks) ||
    !arraySetEquals(currentFilteredCountryCodes.value, queryCountryCodes) ||
    !arraySetEquals(currentFilteredSectors.value, querySectors) ||
    currentSearchBarInput.value !== queryInput
  ) {
    currentFilteredFrameworks.value = queryFrameworks;
    currentFilteredCountryCodes.value = queryCountryCodes;
    currentFilteredSectors.value = querySectors;
    currentSearchBarInput.value = queryInput;
  }
}

/**
 * Called when the new search results are received from the framework search bar. Disables the waiting indicator,
 * resets the pagination and updates the datatable. Also updates the query parameters to reflect the new search parameters
 * @param companiesReceived the received chunk of companies
 * @param chunkIndex the index of the chunk
 * @param totalNumberOfCompanies the total number of companies
 * @returns the promise of the router push with the new query parameters
 */
function handleCompanyQuery(
  companiesReceived: Array<BasicCompanyInformation>,
  chunkIndex: number,
  totalNumberOfCompanies: number
): Promise<NavigationFailure | void | undefined> {
  totalRecords.value = totalNumberOfCompanies;
  resultsArray.value = companiesReceived;

  if (chunkIndex == 0) handlePageUpdate(0);
  waitingForDataToDisplay.value = false;

  const queryInput = currentSearchBarInput.value == '' ? undefined : currentSearchBarInput.value;
  const queryFrameworks = currentFilteredFrameworks.value.length == 0 ? undefined : currentFilteredFrameworks.value;
  const queryCountryCodes =
    currentFilteredCountryCodes.value.length == 0 ? undefined : currentFilteredCountryCodes.value;
  const querySectors = currentFilteredSectors.value.length == 0 ? undefined : currentFilteredSectors.value;

  return router.push({
    name: 'Search Companies for Framework Data',
    query: {
      input: queryInput,
      framework: queryFrameworks,
      countryCode: queryCountryCodes,
      sector: querySectors,
    },
  });
}

/**
 * Called when the user performed a company search. Updates the search bar contents and
 * displays the waiting indicator
 * @param companyNameFilter the new search filter
 */
function handleSearchConfirmed(companyNameFilter: string): void {
  waitingForDataToDisplay.value = true;
  currentSearchBarInput.value = companyNameFilter;
}
</script>

<style scoped>
.search-filters-panel {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  row-gap: var(--spacing-sm);
  width: 100%;
}

.search-filters-panel > div:first-child {
  min-width: 0;
  max-width: 100%;
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
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
}

.collapsed-search-container #frameworkDataSearchBar {
  width: 18rem;
  flex-shrink: 0;
}

.collapsed-search-container .search-filters-panel {
  flex-wrap: nowrap;
  flex-shrink: 0;
  width: auto;
}

.collapsed-search-container .search-filters-panel > div:first-child {
  flex-shrink: 0;
}

.collapsed-search-container #frameworkDataSearchFilters :deep(.filter) {
  flex-wrap: nowrap;
}

.collapsed-search-container #frameworkDataSearchFilters :deep(.filter-group-items) {
  flex-wrap: nowrap;
}

.collapsed-search-container #frameworkDataSearchFilters :deep(.search-filter),
.collapsed-search-container #frameworkDataSearchFilters :deep(.framework-filter) {
  flex-shrink: 0;
}

.button-container {
  padding: 0 var(--spacing-xs);
  display: flex;
  flex-direction: row;
  gap: var(--spacing-md);
  align-items: center;
}

@media (max-width: 992px) {
  .search-bar-and-filters-container #frameworkDataSearchBar {
    width: 100%;
  }

  #frameworkDataSearchFilters {
    width: 100%;
    max-width: 100%;
  }
}
</style>
