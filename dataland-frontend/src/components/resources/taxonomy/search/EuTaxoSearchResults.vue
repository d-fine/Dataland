<template>
  <MarginWrapper bgClass="surface-800">
    <div class="grid align-items-center pr-2">
      <div class="col-1 text-left">
        <h2>Results</h2>
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left">
        <DataTable
          v-if="data && data.length > 0"
          :value="data"
          responsive-layout="scroll"
          :paginator="true"
          :rows="100"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
          :alwaysShowPaginator="false"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
          @update:first="scrollToTop"
          @row-click="goToData"
          class="table-cursor"
          id="search-result-taxonomy"
          :rowHover="true"
        >
          <Column
            field="companyInformation.companyName"
            header="COMPANY"
            :sortable="true"
            class="d-bg-white w-3 d-datatable-column-left"
          >
          </Column>
          <Column field="companyInformation.permId" :sortable="false" class="d-bg-white w-2">
            <template #header>
              <span class="uppercase">PERM ID</span>
              <i
                class="material-icons pl-2"
                aria-hidden="true"
                title="Perm ID"
                v-tooltip.top="{
                  value:
                    'Permanent Identifier (PermID) is a machine readable identifier that provides a unique reference ' +
                    'for data items including organizations, instruments, funds, issuers and people. You can search and verify an id at permid.org/search',
                  class: 'd-tooltip-mw25',
                }"
                >info</i
              >
            </template>
            <template #body="{ data }">
              {{ data.permId ? data.permId : "Not available" }}
            </template>
          </Column>
          <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="d-bg-white w-2"> </Column>
          <Column
            field="companyInformation.marketCap"
            header="MARKET CAP"
            headerClass="d-justify-content-end-inner"
            :sortable="true"
            class="d-bg-white w-1 text-right"
          >
            <template #body="{ data }">
              {{ orderOfMagnitudeSuffix(data.companyInformation.marketCap) }}
            </template>
          </Column>
          <Column field="companyInformation.headquarters" header="LOCATION" :sortable="true" class="d-bg-white w-2">
            <template #body="{ data }">
              {{ buildLocationString(data.companyInformation.headquarters, data.companyInformation.countryCode) }}
            </template>
          </Column>
          <Column field="companyId" header="" class="d-bg-white w-1 d-datatable-column-right">
            <template #body="{ data }">
              <router-link
                :to="'/companies/' + data.companyId + '/eutaxonomies'"
                class="text-primary no-underline font-bold"
                ><span> VIEW</span> <span class="ml-3">></span>
              </router-link>
            </template>
          </Column>
        </DataTable>
        <p v-else>
          The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
      </div>
    </div>
  </MarginWrapper>
</template>

<script>
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import Tooltip from "primevue/tooltip";

export default {
  name: "EuTaxoSearchResults",
  components: { MarginWrapper, DataTable, Column },
  directives: {
    tooltip: Tooltip,
  },
  props: {
    data: {
      type: Object,
      default: null,
    },
    processed: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    orderOfMagnitudeSuffix(value) {
      return convertCurrencyNumbersToNotationWithLetters(value, 2) + " €";
    },
    buildLocationString(headquarters, countryCode) {
      return headquarters + ", " + countryCode;
    },
    scrollToTop() {
      window.scrollTo(0, 0);
    },
    goToData(event) {
      const company = event.data.companyId;
      this.$router.push(`/companies/${company}/eutaxonomies`);
    },
  },
};
</script>
<style>
#search-result-taxonomy tr:hover {
  cursor: pointer;
}
#search-result-taxonomy th {
  background: white;
}

.d-justify-content-end-inner > div {
  justify-content: end;
}
</style>
