<template>
  <div v-if="isLoading" class="d-center-div text-center px-7 py-4">
    <h1>Loading portfolio data...</h1>
    <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-else-if="isError" class="d-center-div text-center px-7 py-4">
    <h1>Error loading portfolio data</h1>
    An unexpected error occurred. Please try again later or with [Ctrl] + [F5] or contact the support team if the issue
    persists.
  </div>
  <div v-else>
    <h1>Portfolio Details of {{ enrichedPortfolio?.portfolioName }}</h1>
    <span class="button_bar">
      <Dropdown
        v-model="selectedFramework"
        :options="frameworks"
        optionLabel="label"
        class="selection-button flex flex-row align-items-center"
        style="width: 16em; display: inline-flex !important"
        :data-test="'framework-dropdown'"
      >
        <template #dropdownicon>
          <svg class="ml-2" xmlns="http://www.w3.org/2000/svg" width="10" height="7" xml:space="preserve">
            <polygon points="0,0 5,5 10,0" fill="currentColor" />
          </svg>
        </template>
      </Dropdown>
      <PrimeButton class="primary-button">Request missing data</PrimeButton>
      <PrimeButton class="primary-button"> <i class="material-icons pr-2">edit</i> Edit Portfolio </PrimeButton>
      <button class="tertiary-button" data-test="reset-filter" @click="resetFilters()">Reset Filter</button>
    </span>

    <DataTable
      stripedRows
      removableSort
      v-model:filters="filters"
      filterDisplay="menu"
      :value="selectedDetails"
      tableStyle="min-width: 50rem"
    >
      <template #empty>
        Currently there are no companies in your portfolio or no companies match your filters. Edit the portfolio to add
        companies or remove filter criteria.
      </template>
      <Column :sortable="true" field="companyName" header="Company Name" :showFilterMatchModes="false">
        <template #body="company">
          <a :href="`/companies/${company.data.companyId}`">{{ company.data.companyName }}</a>
        </template>
        <template #filter="{ filterModel, filterCallback }">
          <InputText
            v-model="filterModel.value"
            type="text"
            @input="filterCallback()"
            placeholder="Filter by company name"
            :data-test="'companyNameFilterValue'"
          />
        </template>
      </Column>
      <Column :sortable="true" field="countryCode" header="Country" :showFilterMatchModes="false">
        <template #filter="{ filterModel, filterCallback }">
          <InputText
            v-model="filterModel.value"
            type="text"
            @input="filterCallback()"
            placeholder="Filter by country"
            :data-test="'countryCodeFilterValue'"
          />
        </template>
      </Column>
      <Column :sortable="true" field="sector" header="Sector" :showFilterMatchModes="false">
        <template #filter="{ filterModel, filterCallback }">
          <InputText
            v-model="filterModel.value"
            type="text"
            @input="filterCallback()"
            placeholder="Filter by sector"
            :data-test="'sectorFilterValue'"
          />
        </template>
      </Column>
      <Column
        :sortable="true"
        field="latestReportingPeriod"
        header="Last Reporting Period"
        :showFilterMatchModes="false"
      >
        <template #body="company">
          <a :href="linkTarget(company.data)">{{ company.data.latestReportingPeriod || 'No data available' }}</a>
        </template>
        <template #filter="{ filterModel, filterCallback }">
          <InputText
            v-model="filterModel.value"
            type="text"
            @input="filterCallback()"
            placeholder="Filter by last reporting period"
            :data-test="'latestReportingPeriodeFilterValue'"
          />
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject, watch } from 'vue';
import type { EnrichedPortfolio, EnrichedPortfolioEntry } from '@clients/userservice';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import Dropdown from 'primevue/dropdown';
import PrimeButton from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import { FilterMatchMode } from 'primevue/api';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { groupBy } from '@/utils/ArrayUtils.ts';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const filters = ref({
  companyName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  countryCode: { value: null, matchMode: FilterMatchMode.CONTAINS },
  sector: { value: null, matchMode: FilterMatchMode.CONTAINS },
  latestReportingPeriod: { value: null, matchMode: FilterMatchMode.CONTAINS },
});

const props = defineProps<{
  portfolioId: string;
}>();

const enrichedPortfolio = ref<EnrichedPortfolio>();
let groupedEntries = {} as Map<string, EnrichedPortfolioEntry[]>;
const frameworks = ref<{ label: string; value: string }[]>([]);
const selectedFramework = ref<{ label: string; value: string }>();
const selectedDetails = ref([] as EnrichedPortfolioEntry[]);
const isLoading = ref(true);
const isError = ref(false);

onMounted(() => {
  isLoading.value = true;
  apiClientProvider.apiClients.portfolioController
    .getEnrichedPortfolio(props.portfolioId)
    .then((response) => {
      enrichedPortfolio.value = response.data;
      groupedEntries = groupBy(enrichedPortfolio.value.entries, (item) => item.framework || '');
      frameworks.value = getFrameworkListSorted();
      selectedFramework.value = frameworks.value[0];
    })
    .catch((reason) => {
      console.error(reason);
      isError.value = true;
    })
    .finally(() => (isLoading.value = false));
});

watch([selectedFramework, enrichedPortfolio], () => {
  selectedDetails.value = groupedEntries.get(selectedFramework?.value?.value || '') || [];
});

/**
 * Resets all filters
 */
function resetFilters(): void {
  let filterName: keyof typeof filters.value;
  for (filterName in filters.value) {
    filters.value[filterName].value = null;
  }
}

/**
 * Generates a link target to the reporting framework or to request new data
 * @param entry The portfolio entry for which the link is generated
 */
function linkTarget(entry: EnrichedPortfolioEntry): string {
  return (
    entry.frameworkDataRef ??
    (entry.latestReportingPeriod
      ? `/companies/${entry.companyId}/frameworks/${entry.framework}`
      : `/singledatarequest/${entry.companyId}?preSelectedFramework=${entry.framework}`)
  );
}

/**
 * Uses a list of data meta info to derive all distinct frameworks that occur in that list. Only if those distinct
 * frameworks are also included in the frontend constant which contains all frameworks that have view-pages
 * implemented, the distinct frameworks are set as options for the framework-dropdown element.
 */
function getFrameworkListSorted(): { label: string; value: string }[] {
  return Array.from(groupedEntries.keys())
    .sort((a, b) => a.localeCompare(b))
    .map((dataType) => {
      return {
        label: humanizeStringOrNumber(dataType),
        value: dataType,
      };
    });
}
</script>

<style lang="scss">
.p-inputtext {
  background: none;
}

.selection-button {
  background: white;
  color: #5a4f36;
  border: 2px solid #5a4f36;
  border-radius: 0.5em;
  height: 2.25rem;
}

.button_bar {
  display: flex;
  margin: 1rem;
  gap: 1rem;

  :last-child {
    margin-left: auto;
  }
}
</style>
