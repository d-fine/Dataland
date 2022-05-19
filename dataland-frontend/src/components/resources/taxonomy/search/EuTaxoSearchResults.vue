<template>
  <MarginWrapper bgClass="surface-800">
    <div class="grid align-items-center pr-2">
      <div class="col-1 text-left">
        <h2>Results</h2>
      </div>
    </div>
    <div class="grid">
      <div class="col-12 text-left">
        <DataTable v-if="data" :value="data" responsive-layout="scroll" :paginator="true" :rows="100"
                   paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport"
                   currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
        >
          <Column field="companyInformation.companyName" header="COMPANY" :sortable="true"
                  class="surface-0 w-3 d-datatable-column-left">
          </Column>
          <Column field="companyInformation.permId" :sortable="false" class="surface-0 w-1">
            <template #header>
              <span class="uppercase">perm ID</span> <i class="material-icons pl-2" aria-hidden="true">info</i>
            </template>
            <template #body="{data}">
              {{data.permId}}
            </template>
          </Column>
          <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="surface-0 w-2">
          </Column>
          <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0 w-2">
            <template #body="{data}">
              {{ orderOfMagnitudeSuffix(data.companyInformation.marketCap) }}
            </template>
          </Column>
          <Column field="companyInformation.headquarters" header="LOCATION" :sortable="true" class="surface-0 w-2">
          </Column>
          <Column field="companyId" header="" class="surface-0 w-2 d-datatable-column-right">
            <template #body="{data}">
              <router-link :to="'/companies/' + data.companyId + '/eutaxonomies'"
                           class="text-primary no-underline font-bold"><span> VIEW</span> <span class="ml-3">></span>
              </router-link>
            </template>
          </Column>
        </DataTable>
        <p v-else>The resource you requested does not exist yet. You can create it:
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
import {numberFormatter} from "@/utils/currencyMagnitude";
import Tooltip from 'primevue/tooltip';

export default {
  name: "EuTaxoSearchResults",
  components: {MarginWrapper, DataTable, Column},
  props: {
    data: {
      type: Object,
      default: null
    },
    processed: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    orderOfMagnitudeSuffix(value) {
      return numberFormatter(value, 2) + " €"
    }
  }
}
</script>