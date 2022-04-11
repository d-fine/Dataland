<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-8 text-left">
        <p v-if="responseArray && responseArray.length > 0" class="text-primary">Select company or <a class="font-semibold text-primary no-underline" @click="filter=true; table=true" href="#">View all ({{responseArray.length}}) results. </a></p>
      <span class="p-fluid">
        <span class="p-input-icon-left p-input-icon-right ">
            <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                  <i v-if="loading" class="pi pi-spinner spin" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                  <i v-else aria-hidden="true"/>
            <AutoComplete v-model="selectedCompany" :suggestions="filteredCompaniesBasic"
                          @complete="searchCompany($event)" placeholder="Search a company by name" inputClass="something"
                          field="companyName" style="z-index:10"

                          @keyup.enter="filter=true; table=true" @item-select="filter=false; table=true"/>
        </span>
      </span>
      </div>
      <div class="col-2 text-left">
        <Button @click="getCompanyByName(true)" label="Search" class="uppercase p-button-sm"><i
            class="material-icons pr-2" aria-hidden="true" id="eu_taxonomy_search_button">search</i> Search
        </Button>
      </div>
    </div>
  </MarginWrapper>
  <DataTable v-if="table" :value="filter ? responseArray : [selectedCompany] "  responsive-layout="scroll" paginator="true" :alwaysShowPaginator="false" rows="5">
    <Column field="companyInformation.companyName" header="COMPANY" :sortable="true" class="surface-0" >
    </Column>
    <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="surface-0"> </Column>
    <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0"> </Column>
    <Column field="companyId" header="" class="surface-0"> <template #body="{data}">
      <router-link :to="'/companies/' + data.companyId + '/eutaxonomies'" class="text-primary no-underline font-bold"> <span> VIEW</span> <span class="ml-3">></span></router-link>
    </template> </Column>
  </DataTable>
  <div v-if="processed && response">
    <EuTaxoSearchResults :data="response.data" :processed="processed"/>
  </div>
</template>

<script>
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import AutoComplete from 'primevue/autocomplete';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.getCompaniesByName, contactSchema)


import Button from "primevue/button";
import EuTaxoSearchResults from "@/components/ui/EuTaxoSearchResults";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "EuTaxoSearchBar",
  components: {MarginWrapper, EuTaxoSearchResults, AutoComplete,  Button, DataTable, Column},
  data() {
    return {
      processed: false,
      table:false,
      responseArray: null,
      filter: false,
      loading: false,
      model: {},
      response: null,
      companyInformation: null,
      selectedCompany: null,
      filteredCompanies: null,
      filteredCompaniesBasic: null,
      additionalCompanies: null
    }
  },
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.processed = false
        if (all) {
          this.model = ""
        }
        this.response = await dataStore.perform(this.model)

      } catch (error) {
        console.error(error)
        this.response = null
      } finally {
        this.processed = true
      }
    },
    async searchCompany(event) {
      try {
        this.loading = true
        this.responseArray = await dataStore.perform(event.query).then(response => {
              return response.data.map(e => ({
                "companyName": e.companyInformation.companyName,
                "companyInformation": e.companyInformation,
                "companyId": e.companyId
              }))
              // hier muss noch viel logic rein und diese zusätzlichen Elemente die noch geladen sind sollten in einem extra feld als link gerendert werden
              // splitting der arrays und dann den rest ensprechend gestylt und an eine bestimmte stelle gehängt
            }
        )
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
        this.additionalCompanies = this.responseArray.slice(0)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>