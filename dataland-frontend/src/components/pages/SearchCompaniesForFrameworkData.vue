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
        :applied-search-string="appliedSearch.searchString"
        :filter="appliedDropdownFilters"
        @search-confirmed="handleSearchConfirmed"
      />

      <div class="search-filters-panel">
        <div>
          <FrameworkDataSearchFilters
            id="frameworkDataSearchFilters"
            class="col-8"
            ref="frameworkDataSearchFilters"
            :selected-country-codes="appliedSearch.countryCodes"
            :selected-frameworks="appliedSearch.dataTypes"
            :selected-sectors="appliedSearch.sectors"
            :selected-reporting-periods="appliedSearch.reportingPeriods"
            @update:selected-country-codes="applyUserSearch({ countryCodes: $event })"
            @update:selected-frameworks="applyUserSearch({ dataTypes: $event })"
            @update:selected-sectors="applyUserSearch({ sectors: $event })"
            @update:selected-reporting-periods="applyUserSearch({ reportingPeriods: $event })"
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

    <div v-if="areCompaniesError && !companies" class="d-center-div text-center px-7 py-4" data-test="companiesError">
      <p class="font-medium text-xl">The companies could not be loaded.</p>
      <p class="font-medium text-xl">Please try again later or contact the support team if the issue persists.</p>
    </div>
    <div v-else-if="isWaitingForFirstResults" class="d-center-div text-center px-7 py-4">
      <p class="font-medium text-xl">Loading...</p>
      <DatalandProgressSpinner />
    </div>
    <FrameworkDataSearchResults
      v-else
      :total-records="totalRecords"
      :previous-records="previousRecords"
      :rows-per-page="ROWS_PER_PAGE"
      :data="companies"
      :loading="areCompaniesPlaceholderData"
      @page-update="handlePageUpdate"
    />
  </TheContent>
</template>

<script setup lang="ts">
import { type CompanySearchFilter } from '@/api-queries/backend/company-data/companyDataKeys';
import { useGetCompaniesQuery } from '@/api-queries/backend/company-data/useGetCompaniesQuery';
import { useGetNumberOfCompaniesQuery } from '@/api-queries/backend/company-data/useGetNumberOfCompaniesQuery';
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkDataSearchBar from '@/components/resources/frameworkDataSearch/FrameworkDataSearchBar.vue';
import FrameworkDataSearchFilters from '@/components/resources/frameworkDataSearch/FrameworkDataSearchFilters.vue';
import FrameworkDataSearchResults from '@/components/resources/frameworkDataSearch/FrameworkDataSearchResults.vue';
import router from '@/router';
import { arraySetEquals } from '@/utils/ArrayUtils';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { parseQueryParamArray } from '@/utils/QueryParserUtils';
import { useCurrentUser } from '@/utils/useCurrentUser';
import { DataTypeEnum } from '@clients/backend';
import { keepPreviousData } from '@tanstack/vue-query';
import PrimeButton from 'primevue/button';
import { computed, onUnmounted, ref } from 'vue';
import { type RouteLocationNormalizedLoaded, onBeforeRouteUpdate, useRoute } from 'vue-router';

type AppliedCompanySearch = Required<CompanySearchFilter>;

interface FrameworkDataSearchBarRef {
  closeOverlay?: () => void;
}

interface FrameworkDataSearchFiltersRef {
  closeAllOpenDropDowns?: () => void;
}

const ROWS_PER_PAGE = 100;

const frameworkDataSearchFilters = ref<FrameworkDataSearchFiltersRef | null>(null);
const frameworkDataSearchBar = ref<FrameworkDataSearchBarRef | null>(null);

const route = useRoute();
const { roles: currentUserRoles } = useCurrentUser();
const hasUserUploaderRights = computed(() => currentUserRoles.value.includes(KEYCLOAK_ROLE_UPLOADER));

const isSearchBarContainerCollapsed = ref(false);
const appliedSearch = ref<AppliedCompanySearch>({ ...getSearchFromRoute(route), reportingPeriods: [] });
const currentPage = ref(0);

const appliedDropdownFilters = computed(() => ({
  dataTypes: appliedSearch.value.dataTypes,
  countryCodes: appliedSearch.value.countryCodes,
  sectors: appliedSearch.value.sectors,
  reportingPeriods: appliedSearch.value.reportingPeriods,
}));

const {
  data: companies,
  isPending: areCompaniesPending,
  isPlaceholderData: areCompaniesPlaceholderData,
  isError: areCompaniesError,
} = useGetCompaniesQuery(
  {
    searchString: () => appliedSearch.value.searchString,
    dataTypes: () => appliedSearch.value.dataTypes,
    countryCodes: () => appliedSearch.value.countryCodes,
    sectors: () => appliedSearch.value.sectors,
    reportingPeriods: () => appliedSearch.value.reportingPeriods,
    chunkSize: ROWS_PER_PAGE,
    chunkIndex: currentPage,
  },
  { placeholderData: keepPreviousData }
);

// Without any search or filter, only companies with data for at least one framework are counted
const isAnySearchOrFilterApplied = computed(
  () =>
    appliedSearch.value.searchString.length +
      appliedSearch.value.dataTypes.length +
      appliedSearch.value.countryCodes.length +
      appliedSearch.value.sectors.length +
      appliedSearch.value.reportingPeriods.length >
    0
);
const {
  data: numberOfCompanies,
  isPending: isNumberOfCompaniesPending,
  isPlaceholderData: isNumberOfCompaniesPlaceholderData,
} = useGetNumberOfCompaniesQuery(
  {
    searchString: () => appliedSearch.value.searchString,
    dataTypes: () => (isAnySearchOrFilterApplied.value ? appliedSearch.value.dataTypes : Object.values(DataTypeEnum)),
    countryCodes: () => appliedSearch.value.countryCodes,
    sectors: () => appliedSearch.value.sectors,
    reportingPeriods: () => appliedSearch.value.reportingPeriods,
  },
  { placeholderData: keepPreviousData }
);

const totalRecords = computed(() => numberOfCompanies.value ?? 0);
const previousRecords = computed(() => currentPage.value * ROWS_PER_PAGE);
const isWaitingForFirstResults = computed(
  () => areCompaniesPending.value || (areCompaniesPlaceholderData.value && !companies.value?.length)
);

const currentlyVisiblePageText = computed((): string => {
  const totalSearchResults = totalRecords.value;
  if (
    areCompaniesPending.value ||
    areCompaniesPlaceholderData.value ||
    isNumberOfCompaniesPending.value ||
    isNumberOfCompaniesPlaceholderData.value
  ) {
    return 'loading...';
  } else if (totalSearchResults === 0) {
    return 'No results';
  } else {
    const startIndex = currentPage.value * ROWS_PER_PAGE;
    const endIndex =
      startIndex + (ROWS_PER_PAGE - 1) >= totalSearchResults
        ? totalSearchResults - 1
        : startIndex + (ROWS_PER_PAGE - 1);
    return `${startIndex + 1}-${endIndex + 1} of ${totalSearchResults} results`;
  }
});

// Searches changed by a navigation (e.g. the back button) are applied without writing the URL again
onBeforeRouteUpdate((to) => {
  applySearch(getSearchFromRoute(to));
});

globalThis.addEventListener('scroll', handleScroll);
handleScroll();

onUnmounted(() => {
  globalThis.removeEventListener('scroll', handleScroll);
});

/**
 * Applies a change of the search string or the filters. A changed search starts at the first page.
 * @param change the changed parts of the search
 * @returns true if the search changed
 */
function applySearch(change: Partial<AppliedCompanySearch>): boolean {
  const current = appliedSearch.value;
  const next = { ...current, ...change };
  if (
    next.searchString === current.searchString &&
    arraySetEquals(next.dataTypes, current.dataTypes) &&
    arraySetEquals(next.countryCodes, current.countryCodes) &&
    arraySetEquals(next.sectors, current.sectors) &&
    arraySetEquals(next.reportingPeriods, current.reportingPeriods)
  ) {
    return false;
  }
  appliedSearch.value = next;
  currentPage.value = 0;
  return true;
}

/**
 * Applies a change of the search string or the filters made by the user and writes the changed search to the URL.
 * Reporting periods are not part of the URL.
 * @param change the changed parts of the search
 */
function applyUserSearch(change: Partial<AppliedCompanySearch>): void {
  if (!applySearch(change)) return;
  const search = appliedSearch.value;
  void router.push({
    name: 'Search Companies for Framework Data',
    query: {
      input: search.searchString || undefined,
      framework: search.dataTypes.length ? search.dataTypes : undefined,
      countryCode: search.countryCodes.length ? search.countryCodes : undefined,
      sector: search.sectors.length ? search.sectors : undefined,
    },
  });
}

/**
 * Called when the user confirmed a search in the search bar. Applies the search string and shows the first page.
 * @param searchString the confirmed search string
 */
function handleSearchConfirmed(searchString: string): void {
  applyUserSearch({ searchString });
  currentPage.value = 0;
}

/**
 * Redirects to the new dataset page
 */
function linkToNewDatasetPage(): void {
  void router.push('/companies/choose');
}

/**
 * Shows another page of the search results
 * @param pageNumber the new page index
 */
function handlePageUpdate(pageNumber: number): void {
  currentPage.value = pageNumber;
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
 * Reads the search string and the framework, country-code and sector filters from the query parameters of a route.
 * @param route the route to read
 * @returns the search described by the route
 */
function getSearchFromRoute(route: RouteLocationNormalizedLoaded): Omit<AppliedCompanySearch, 'reportingPeriods'> {
  const allowedDataTypeEnumValues = FRAMEWORKS_WITH_VIEW_PAGE as Array<string>;
  return {
    searchString: (route.query.input as string) || '',
    dataTypes: (route.query.framework ? parseQueryParamArray(route.query.framework) : []).filter((framework) =>
      allowedDataTypeEnumValues.includes(framework)
    ) as Array<DataTypeEnum>,
    countryCodes: route.query.countryCode ? parseQueryParamArray(route.query.countryCode) : [],
    sectors: route.query.sector ? parseQueryParamArray(route.query.sector) : [],
  };
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
