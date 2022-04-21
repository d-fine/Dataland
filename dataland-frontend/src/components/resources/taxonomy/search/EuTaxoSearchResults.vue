<template>
  <MarginWrapper bgClass="surface-800">
    <div class="grid align-items-center pr-2">
      <div class="col-1 text-left">
        <h2>Results</h2>
      </div>
      <div class="col-3 col-offset-8 surface-0 d-card">
        <div class="grid align-items-center h-3rem ">
          <div class="col-6 text-left">
            <span class="font-semibold">Avg Green asset ratio </span>
          </div>
          <div class="col-6 text-right text-green-500">
            <span class="font-semibold text-xl">78</span> <span> % </span>
          </div>
        </div>
      </div>
      </div>
    <div class="grid">
      <div class="col-12 text-left">
          <DataTable v-if="data" :value="data" responsive-layout="scroll">
            <Column field="companyInformation.companyName" header="COMPANY" :sortable="true" class="surface-0 w-3 d-datatable-column-left">
            </Column>
            <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="surface-0 w-3">
            </Column>
            <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0 w-2">
              <template #body="{data}">
                {{orderOfMagnitudeSuffix(data.companyInformation.marketCap)}}
              </template>
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
      orderOfMagnitudeSuffix(value){
        return numberFormatter(value,2) + " €"
      }
    }
}
</script>

<style>
.p-datatable-thead .d-datatable-column-left {
  border-top-left-radius: 8px;
}

.p-datatable-thead .d-datatable-column-right {
  border-top-right-radius: 8px;
}
</style>