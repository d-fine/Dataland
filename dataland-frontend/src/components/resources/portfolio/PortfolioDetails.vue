<template>
  <h1>Portfolio Details of {{ props.portfolio.portfolioName }}</h1>
  <span class="button_bar">
    <Dropdown
      v-model="selectedFramework"
      :options="getFrameworkListSorted(portfolio.frameworks)"
      optionLabel="label"
      class="selection-button flex flex-row align-items-center"
      style="width: 16em; display: inline-flex !important"
    >
      <template #dropdownicon>
        <svg class="ml-2" xmlns="http://www.w3.org/2000/svg" width="10" height="7" xml:space="preserve">
          <polygon points="0,0 5,5 10,0" fill="currentColor" />
        </svg>
      </template>
    </Dropdown>
    <PrimeButton class="primary-button">Request missing data</PrimeButton>
    <PrimeButton class="primary-button"> <i class="material-icons pr-2">edit</i> Edit Portfolio </PrimeButton>
    <button class="tertiary-button" data-test="reset-filter" @click="filters.id.value = null">Reset Filter</button>
  </span>

  <DataTable
    stripedRows
    removableSort
    v-model:filters="filters"
    filterDisplay="menu"
    :value="
      Array.from(portfolio.companyIds).map((id) => {
        return { id: id };
      })
    "
    tableStyle="min-width: 50rem"
  >
    <template #empty>
      Currently there are no companies in your portfolio or no companies match your filters. Edit the portfolio to add
      companies or remove filter criteria.
    </template>
    <Column :sortable="true" field="id" header="Company ID" style="width: 25em" :showFilterMatchModes="false">
      <template #body="company">
        <a :href="`/companies/${company.data.id}`">{{ company.data.id }}</a>
      </template>
      <template #filter="{ filterModel, filterCallback }">
        <InputText v-model="filterModel.value" type="text" @input="filterCallback()" placeholder="Search by ID" />
      </template>
    </Column>
  </DataTable>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { BasePortfolio, BasePortfolioFrameworksEnum } from '@clients/userservice';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import Dropdown from 'primevue/dropdown';
import PrimeButton from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import InputText from 'primevue/inputtext';
import { FilterMatchMode } from 'primevue/api';

const filters = ref({
  id: { value: null, matchMode: FilterMatchMode.CONTAINS },
});

const props = defineProps<{
  portfolio: BasePortfolio;
}>();

const selectedFramework = ref(getFrameworkListSorted(props.portfolio.frameworks)[0]);

/**
 * Uses a list of data meta info to derive all distinct frameworks that occur in that list. Only if those distinct
 * frameworks are also included in the frontend constant which contains all frameworks that have view-pages
 * implemented, the distinct frameworks are set as options for the framework-dropdown element.
 * @param frameworks an array of data meta info
 */
function getFrameworkListSorted(frameworks: Set<BasePortfolioFrameworksEnum>): { label: string; value: string }[] {
  return Array.from(frameworks)
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
  margin: 0.25rem 1rem;
  gap: 1rem;

  :last-child {
    margin-left: auto;
  }
}
</style>
