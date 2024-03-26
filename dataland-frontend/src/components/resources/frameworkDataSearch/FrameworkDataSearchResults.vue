<template>
  <div class="col-12 text-left">
    <DataTable
      v-if="data && data.length > 0"
      ref="dataTable"
      :value="data"
      :paginator="true"
      :rows="rowsPerPage"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
      :alwaysShowPaginator="false"
      currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
      @update:first="firstUpdated"
      @row-click="goToCompanyCockpit"
      class="table-cursor"
      id="search-result-framework-data"
      :rowHover="true"
    >
      <Column field="companyName" header="COMPANY" :sortable="false" class="d-bg-white w-3 d-datatable-column-left">
      </Column>
      <Column field="lei" :sortable="false" class="d-bg-white w-2">
        <template #header>
          <span class="uppercase">Lei</span>
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
          {{ data.lei ? data.lei : "Not available" }}
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
    <div class="d-center-div text-center px-7 py-4" v-else>
      <p class="font-medium text-xl">We're sorry, but your search did not return any results.</p>
      <p class="font-medium text-xl">
        Please double-check the spelling and try again or request the data you are missing!
      </p>
      <BulkDataRequestButton />
    </div>
  </div>
</template>

<script lang="ts">
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import Tooltip from "primevue/tooltip";
import { defineComponent } from "vue";
import BulkDataRequestButton from "@/components/resources/frameworkDataSearch/BulkDataRequestButton.vue";
import { type BasicCompanyInformation } from "@clients/backend";

export default defineComponent({
  name: "FrameworkDataSearchResults",
  components: { BulkDataRequestButton, DataTable, Column },
  directives: {
    tooltip: Tooltip,
  },
  props: {
    data: {
      type: Object,
      default: null,
    },
    rowsPerPage: {
      type: Number,
      default: null,
    },
  },
  methods: {
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
    /**
     * Resets the pagination of the dataTable
     */
    // The following method is used, the linter reports a false positive here
    // eslint-disable-next-line vue/no-unused-properties
    resetPagination() {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-call
      if (this.$refs.dataTable) this.$refs.dataTable.resetPage();
    },
    /**
     * Called when the id of the first row is updated (i.e. when the user navigates to the next page)
     * Scrolls back to the top and propagates the event
     * @param event the new number of the first row
     */
    firstUpdated(event: never) {
      window.scrollTo(0, 0);
      this.$emit("update:first", event);
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
