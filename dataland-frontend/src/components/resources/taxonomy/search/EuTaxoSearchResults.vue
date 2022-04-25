<template>
  <MarginWrapper bgClass="surface-800">
    <div class="grid align-items-center pr-2">
      <div class="col-1 text-left">
        <h2>Results</h2>
      </div>
      <div v-if="false" class="col-4 col-offset-7 surface-0 d-card" v-tooltip.top="eligibleRevenueTooltip">
        <div class="grid align-items-center h-3rem ">
          <div class="col-9 text-left">
            <span class="font-semibold">Avg. EU Taxonomy Eligible Revenue </span>
          </div>
          <div class="col-3 text-right text-green-500">
            <span class="font-semibold text-xl">78</span> <span> % </span>
          </div>
        </div>
      </div>
      </div>
    <div class="grid">
      <div class="col-12 text-left">
          <DataTable v-if="data" :value="data" responsive-layout="scroll" :paginator="true" :rows="100"
                     paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport" currentPageReportTemplate="Showing {first} to {last} of {totalRecords} entries"
          >
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
import Tooltip from 'primevue/tooltip';
export default {
  name: "EuTaxoSearchResults",
  components: {MarginWrapper, DataTable, Column},
  directives: {
    'tooltip': Tooltip
  },
  props: {
    eligibleRevenueTooltip:
        { type: Object,
          default() {
            return {
              value: 'The NFRD (Non financial disclosure directive) applies to companies ' +
                      'with more than 500 employees with a  >€20M balance or >€40M net turnover',
              class: 'd-tooltip'
            }
          }
        },
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


</style>