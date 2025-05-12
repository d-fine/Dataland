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
      <PrimeButton class="primary-button" @click="editPortfolio()" data-test="edit-portfolio">
        <i class="material-icons pr-2">edit</i> Edit Portfolio
      </PrimeButton>
      <PrimeButton class="primary-button" @click="downloadPortfolio()" data-test="download-portfolio">
        <i class="pi pi-download pr-2" /> Download Portfolio
      </PrimeButton>
      <button class="tertiary-button" data-test="reset-filter" @click="resetFilters()">Reset Filter</button>
    </span>

    <DataTable
      stripedRows
      removableSort
      v-model:filters="filters"
      filterDisplay="menu"
      :value="selectedDetails"
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
        :sortable="true"
        field="latestReportingPeriod"
        header="Last Reporting Period"
        :showFilterMatchModes="false"
      >
        <template #body="portfolioEntry">
          <a v-if="portfolioEntry.data.frameworkDataRef" :href="portfolioEntry.data.frameworkDataRef">{{
            portfolioEntry.data.latestReportingPeriod
          }}</a>
          <span v-else>{{ portfolioEntry.data.latestReportingPeriod }}</span>
        </template>
        <template #filter="{ filterModel, filterCallback }">
          <div v-for="category of reportingPeriodOptions" :key="category" class="filter-checkbox">
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
import { ref, onMounted, inject, watch, h } from 'vue';
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
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import { useDialog } from 'primevue/usedialog';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter.ts';
import Checkbox from 'primevue/checkbox';
import PortfolioDownload from '@/components/resources/portfolio/PortfolioDownload.vue';

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
  readonly framework: string;
  readonly companyCockpitRef: string;
  readonly frameworkDataRef?: string;
  readonly latestReportingPeriod?: string;

  constructor(portfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = portfolioEntry.companyId;
    this.companyName = portfolioEntry.companyName;
    this.sector = portfolioEntry.sector;
    this.country = getCountryNameFromCountryCode(portfolioEntry.countryCode) ?? 'unknown';
    this.framework = portfolioEntry.framework ?? '';
    this.companyCockpitRef = portfolioEntry.companyCockpitRef;
    this.frameworkDataRef =
      portfolioEntry.frameworkDataRef ||
      (portfolioEntry.latestReportingPeriod
        ? `/companies/${portfolioEntry.companyId}/frameworks/${portfolioEntry.framework}`
        : undefined);
    this.latestReportingPeriod = portfolioEntry.latestReportingPeriod || 'No data available';
  }
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const emit = defineEmits(['update:portfolio-overview']);
const reportingPeriodOptions = ref<string[]>([]);

const filters = ref({
  companyName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  country: { value: null, matchMode: FilterMatchMode.CONTAINS },
  sector: { value: null, matchMode: FilterMatchMode.CONTAINS },
  latestReportingPeriod: { value: [], matchMode: FilterMatchMode.IN },
});

const props = defineProps<{
  portfolioId: string;
}>();

const enrichedPortfolio = ref<EnrichedPortfolio>();
let groupedEntries = {} as Map<string, PortfolioEntryPrepared[]>;
const frameworks = ref<{ label: string; value: string }[]>([]);
const selectedFramework = ref<{ label: string; value: string }>();
const selectedDetails = ref([] as PortfolioEntryPrepared[]);
const isLoading = ref(true);
const isError = ref(false);

onMounted(() => {
  loadPortfolio();
});

watch([selectedFramework, enrichedPortfolio], () => {
  const currentFramework = selectedFramework?.value?.value || '';
  const entries = groupedEntries.get(currentFramework) || [];

  selectedDetails.value = entries;

  reportingPeriodOptions.value = Array.from(
    new Set(
      entries
        .map((entry) => entry.latestReportingPeriod)
        .filter((period): period is string => typeof period === 'string')
    )
  ).sort();
});
/**
 * (Re-)loads a portfolio
 */
function loadPortfolio(): void {
  isLoading.value = true;
  apiClientProvider.apiClients.portfolioController
    .getEnrichedPortfolio(props.portfolioId)
    .then((response) => {
      enrichedPortfolio.value = response.data;

      const preparedPortfolioEntries = enrichedPortfolio.value.entries.map((item) => new PortfolioEntryPrepared(item));

      groupedEntries = groupBy(preparedPortfolioEntries, (item) => item.framework);
      frameworks.value = getFrameworkListSorted();
      selectedFramework.value = frameworks.value[0];
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
 * Extracts all distinct frameworks that are supported in the frontend from the metadata.
 * Then the resulting list is sorted alphabetically.
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

/**
 * Opens the PortfolioDownload with the current portfolio's data for downloading.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
 */
function downloadPortfolio(): void {
  const fullName = 'Download ' + enrichedPortfolio.value?.portfolioName;

  dialog.open(PortfolioDownload, {
    props: {
      modal: true,
    },
    templates: {
      header: () => {
        return h(
          'div',
          {
            class: 'p-dialog-title',
            style: {
              maxWidth: '15em',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            },
            title: fullName,
          },
          fullName
        );
      },
    },

    data: {
      portfolioName: fullName,
      portfolio: enrichedPortfolio.value,
      companies: selectedDetails.value,
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
