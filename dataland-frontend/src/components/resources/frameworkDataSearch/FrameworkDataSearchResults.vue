<template>
  <MarginWrapper>
    <div class="grid mt-2">
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
          id="search-result-framework-data"
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
          <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="d-bg-white w-2" />
          <Column field="companyInformation.headquarters" header="LOCATION" :sortable="true" class="d-bg-white w-2">
            <template #body="{ data }">
              {{ buildLocationString(data.companyInformation.headquarters, data.companyInformation.countryCode) }}
            </template>
          </Column>
          <Column field="companyId" header="" class="d-bg-white w-1 d-datatable-column-right">
            <template #body="{ data }">
              <router-link :to="getRouterLinkTargetFrameworkInt(data)" class="text-primary no-underline font-bold"
                ><span> VIEW</span> <span class="ml-3">></span>
              </router-link>
            </template>
          </Column>
        </DataTable>
        <div class="d-center-div text-center px-7 py-4" v-else>
          <p class="font-medium text-xl">Sorry! The company you searched for was not found in our database.</p>
          <p class="font-medium">Try again please!</p>
        </div>
      </div>
    </div>
  </MarginWrapper>
</template>

<style scoped>
.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}
</style>

<script lang="ts">
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import Tooltip from "primevue/tooltip";
import {
  DataSearchStoredCompany,
  getRouterLinkTargetFramework,
} from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";
import { defineComponent } from "vue";

export default defineComponent({
  name: "FrameworkDataSearchResults",
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
    orderOfMagnitudeSuffix(value: number) {
      return convertCurrencyNumbersToNotationWithLetters(value, 2) + " €";
    },
    buildLocationString(headquarters: string, countryCode: string) {
      return headquarters + ", " + countryCode;
    },
    scrollToTop() {
      window.scrollTo(0, 0);
    },
    goToData(event: { data: DataSearchStoredCompany }) {
      void this.$router.push(this.getRouterLinkTargetFrameworkInt(event.data));
    },
    getRouterLinkTargetFrameworkInt(companyData: DataSearchStoredCompany) {
      return getRouterLinkTargetFramework(companyData);
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
