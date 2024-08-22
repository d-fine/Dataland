<template>
  <div class="col-12 text-left">
    <DataTable
      v-if="data && data.length > 0"
      ref="dataTable"
      :value="data"
      :paginator="true"
      @page="onPage($event)"
      :lazy="true"
      :first="previousRecords"
      :total-records="totalRecords"
      :rows="rowsPerPage"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
      :alwaysShowPaginator="false"
      currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
      @row-click="goToCompanyCockpit"
      class="table-cursor"
      id="search-result-framework-data"
      :rowHover="true"
    >
      <Column field="companyName" header="COMPANY" :sortable="false" class="d-bg-white w-3 d-datatable-column-left">
      </Column>
      <Column field="lei" :sortable="false" class="d-bg-white w-2">
        <template #header>
          <span class="uppercase">LEI</span>
          <i
            class="material-icons pl-2"
            aria-hidden="true"
            title="LEI"
            v-tooltip.top="{
              value:
                'The Legal Entity Identifier (LEI) is a 20-character, alpha-numeric code based on the ' +
                'ISO 17442 standard developed by the International Organization for Standardization (ISO).',
              class: 'd-tooltip',
            }"
            >info</i
          >
        </template>
        <template #body="{ data }">
          {{ data.lei ? data.lei : 'Not available' }}
        </template>
      </Column>
      <Column field="sector" header="SECTOR" :sortable="false" class="d-bg-white w-2" />
      <Column field="headquarters" header="LOCATION" :sortable="false" class="d-bg-white w-2">
        <template #body="{ data }"> {{ data.headquarters }}, {{ data.countryCode }} </template>
      </Column>
      <Column field="companyId" header="" class="d-bg-white w-1 d-datatable-column-right">
        <template #body>
          <span class="text-primary no-underline font-bold"><span> VIEW</span> <span class="ml-3">></span> </span>
        </template>
      </Column>
    </DataTable>
    <div class="d-center-div text-center px-7 py-4" v-else data-test="DataSearchNoResultsText">
      <p class="font-medium text-xl">We're sorry, but your search did not return any results.</p>
      <p class="font-medium text-xl">Please double-check the spelling and filter settings!</p>
      <p class="font-medium text-xl">
        It might be possible that the company you searched for does not exist on Dataland yet.
      </p>
    </div>
  </div>
</template>

<script lang="ts">
import DataTable, { type DataTablePageEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import Tooltip from 'primevue/tooltip';
import { defineComponent, type PropType } from 'vue';
import { type BasicCompanyInformation } from '@clients/backend';

export default defineComponent({
  name: 'FrameworkDataSearchResults',
  components: { DataTable, Column },
  emits: ['page-update'],
  directives: {
    tooltip: Tooltip,
  },
  props: {
    data: {
      type: Array as PropType<BasicCompanyInformation[]>,
      default: null,
    },
    previousRecords: {
      type: Number,
      default: null,
    },
    totalRecords: {
      type: Number,
      default: null,
    },
    rowsPerPage: {
      type: Number,
      default: null,
    },
  },
  methods: {
    /**
     * Updates the current Page in the parent component
     * @param event DataTablePageEvent
     */
    onPage(event: DataTablePageEvent) {
      window.scrollTo(0, 0);
      this.$emit('page-update', event.page);
    },
    /**
     * Navigates to the company cockpit page on a click on the row of the company
     * @param event the row click event
     * @param event.data the company the user clicked on
     * @returns the promise of the router push action
     */
    goToCompanyCockpit(event: { data: BasicCompanyInformation }) {
      const companyIdOfClickedRow = event.data.companyId;
      return this.$router.push(`/companies/${companyIdOfClickedRow}`);
    },
  },
});
</script>

<style>
#search-result-framework-data tr:hover {
  cursor: pointer;
}

#search-result-framework-data th {
  background: white;
}

#search-result-framework-data .d-justify-content-end-inner > div {
  justify-content: end;
}
</style>
