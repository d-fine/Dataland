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
    <span class="button_bar">
      <PrimeButton class="primary-button" @click="editPortfolio()" data-test="edit-portfolio">
        <i class="material-icons pr-2">edit</i> Edit Portfolio
      </PrimeButton>
      <button class="tertiary-button" data-test="reset-filter" @click="resetFilters()">Reset Filter</button>
    </span>

    <DataTable
      stripedRows
      removableSort
      v-model:filters="filters"
      filterDisplay="menu"
      :value="portfolioEntriesToDisplay"
      tableStyle="min-width: 50rem"
      sortField="companyName"
      :sortOrder="1"
    >
      <template #empty>
        Currently there are no companies in your portfolio or no companies match your filters. Edit the portfolio to add
        companies or remove filter criteria.
      </template>
      <Column :sortable="true" field="companyName" header="Company Name" :showFilterMatchModes="false">
        <template #body="portfolioEntry">
          <a :href="`/companies/${portfolioEntry.data.companyId}`">{{ portfolioEntry.data.companyName }}</a>
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
      <Column :sortable="true" field="country" header="Country" :showFilterMatchModes="false">
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
        v-for="framework of majorFrameworks"
        :key="framework"
        :sortable="true"
        :field="convertHyphenatedStringToCamelCase(framework) + 'AvailableReportingPeriods'"
        :header="humanizeStringOrNumber(framework)"
        :showFilterMatchModes="false"
      >
        <template #body="portfolioEntry">
          <a
            v-if="portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework)"
            :href="portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework)"
            >{{ getAvailableReportingPeriods(portfolioEntry.data, framework) }}</a
          >
          <span v-else>{{ getAvailableReportingPeriods(portfolioEntry.data, framework) }}</span>
        </template>
        <template #filter="{ filterModel, filterCallback }">
          <div v-for="category of reportingPeriodOptions.get(framework)" :key="category" class="filter-checkbox">
            <Checkbox
              v-model="filterModel.value"
              :inputId="category"
              name="category"
              :value="category"
              :data-test="'latestReportingPeriodeFilterValue'"
              @change="filterCallback"
            />
            <label :for="category">{{ category }}</label>
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject, watch } from 'vue';
import type { EnrichedPortfolio, EnrichedPortfolioEntry } from '@clients/userservice';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import PrimeButton from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import { FilterMatchMode } from 'primevue/api';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import { useDialog } from 'primevue/usedialog';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter.ts';
import Checkbox from 'primevue/checkbox';

const majorFrameworks = ['sfdr', 'eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'];

/**
 * This class prepares raw `EnrichedPortfolioEntry` data for use in UI components
 * by transforming and enriching fields, such as converting country codes to names,
 * resolving fallback values, and constructing framework-related URLs.
 */
class PortfolioEntryPrepared {
  readonly companyId: string;
  readonly companyName: string;
  readonly sector?: string;
  readonly country: string;
  readonly companyCockpitRef: string;
  readonly frameworkHyphenatedNamesToDataRef: Map<string, string | undefined>;
  readonly sfdrAvailableReportingPeriods: string | undefined;
  readonly eutaxonomyFinancialsAvailableReportingPeriods: string | undefined;
  readonly eutaxonomyNonFinancialsAvailableReportingPeriods: string | undefined;
  readonly nuclearAndGasAvailableReportingPeriods: string | undefined;

  constructor(portfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = portfolioEntry.companyId;
    this.companyName = portfolioEntry.companyName;
    this.sector = portfolioEntry.sector;
    this.country = getCountryNameFromCountryCode(portfolioEntry.countryCode) ?? 'unknown';
    this.companyCockpitRef = portfolioEntry.companyCockpitRef;
    this.frameworkHyphenatedNamesToDataRef = new Map<string, string | undefined>();

    majorFrameworks.forEach((framework) => {
      this.frameworkHyphenatedNamesToDataRef.set(
        framework,
        portfolioEntry.frameworkHyphenatedNamesToDataRef[framework] ||
          (portfolioEntry.availableReportingPeriods[framework]
            ? `/companies/${portfolioEntry.companyId}/frameworks/${framework}`
            : undefined)
      );
    });

    this.sfdrAvailableReportingPeriods = portfolioEntry.availableReportingPeriods['sfdr'] || 'No data available';
    this.eutaxonomyFinancialsAvailableReportingPeriods =
      portfolioEntry.availableReportingPeriods['eutaxonomy-financials'] || 'No data available';
    this.eutaxonomyNonFinancialsAvailableReportingPeriods =
      portfolioEntry.availableReportingPeriods['eutaxonomy-non-financials'] || 'No data available';
    this.nuclearAndGasAvailableReportingPeriods =
      portfolioEntry.availableReportingPeriods['nuclear-and-gas'] || 'No data available';
  }
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const emit = defineEmits(['update:portfolio-overview']);
const reportingPeriodOptions = ref<Map<string, string[]>>(new Map<string, string[]>());

const filters = ref({
  companyName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  country: { value: null, matchMode: FilterMatchMode.CONTAINS },
  sector: { value: null, matchMode: FilterMatchMode.CONTAINS },
  sfdrAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
  eutaxonomyFinancialsAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
  eutaxonomyNonFinancialsAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
  nuclearAndGasAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
});

const props = defineProps<{
  portfolioId: string;
}>();

const enrichedPortfolio = ref<EnrichedPortfolio>();
const portfolioEntriesToDisplay = ref([] as PortfolioEntryPrepared[]);
const isLoading = ref(true);
const isError = ref(false);

onMounted(() => {
  loadPortfolio();
});

watch([enrichedPortfolio], () => {
  const entries = portfolioEntriesToDisplay.value || [];

  majorFrameworks.forEach((framework) => {
    reportingPeriodOptions.value.set(
      framework,
      Array.from(
        new Set(
          entries
            .map((entry) => getAvailableReportingPeriods(entry, framework))
            .filter((period): period is string => typeof period === 'string')
        )
      ).sort()
    );
  });
});

/**
 * Convert the given hyphenated string to camel case by deleting each hyphen and capitalizing
 * each letter originally preceded by a hyphen.
 * @param hyphenatedString
 */
function convertHyphenatedStringToCamelCase(hyphenatedString: string): string {
  return hyphenatedString
    .split('-')
    .map((word, index) => (index === 0 ? word : word.charAt(0).toUpperCase() + word.slice(1)))
    .join('');
}

/**
 * For a given prepared portfolio entry and (hyphenated) framework name, return the associated
 * string of available reporting periods.
 * @param portfolioEntryPrepared
 * @param frameworkName
 */
function getAvailableReportingPeriods(
  portfolioEntryPrepared: PortfolioEntryPrepared,
  frameworkName: string
): string | undefined {
  switch (frameworkName) {
    case 'sfdr':
      return portfolioEntryPrepared.sfdrAvailableReportingPeriods;
    case 'eutaxonomy-financials':
      return portfolioEntryPrepared.eutaxonomyFinancialsAvailableReportingPeriods;
    case 'eutaxonomy-non-financials':
      return portfolioEntryPrepared.eutaxonomyNonFinancialsAvailableReportingPeriods;
    case 'nuclear-and-gas':
      return portfolioEntryPrepared.nuclearAndGasAvailableReportingPeriods;
    default:
      return undefined;
  }
}

/**
 * (Re-)loads a portfolio
 */
function loadPortfolio(): void {
  isLoading.value = true;
  apiClientProvider.apiClients.portfolioController
    .getEnrichedPortfolio(props.portfolioId)
    .then((response) => {
      enrichedPortfolio.value = response.data;

      portfolioEntriesToDisplay.value = enrichedPortfolio.value.entries.map((item) => new PortfolioEntryPrepared(item));
    })
    .catch((reason) => {
      console.error(reason);
      isError.value = true;
    })
    .finally(() => (isLoading.value = false));
}

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
 * Opens the PortfolioDialog with the current portfolio's data for editing.
 * Once the dialog is closed, it reloads the portfolio data and emits an update event
 * to refresh the portfolio overview.
 */
function editPortfolio(): void {
  dialog.open(PortfolioDialog, {
    props: {
      header: 'Edit Portfolio',
      modal: true,
    },
    data: {
      portfolio: enrichedPortfolio.value,
    },
    onClose() {
      loadPortfolio();
      emit('update:portfolio-overview');
    },
  });
}
</script>

<style scoped lang="scss">
label {
  margin-left: 0.5em;
}

.filter-checkbox {
  margin: 0.25em 0;
}

a {
  color: var(--primary-color);
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

a:after {
  content: '>';
  margin: 0 0.5em;
  font-weight: bold;
}

:deep(.p-inputtext) {
  background: none;
}

:deep(.p-column-filter) {
  margin: 0.5rem;
}

:deep(.p-datatable .p-sortable-column .p-sortable-column-icon) {
  color: inherit;
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
