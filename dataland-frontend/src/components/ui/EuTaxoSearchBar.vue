<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-8 text-left">
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right ">
              <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                    <i v-if="loading" class="pi pi-spinner spin" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                    <i v-else aria-hidden="true"/>
              <AutoComplete v-model="selectedCompany" :suggestions="filteredCompaniesBasic"
                            @complete="searchCompany($event)" placeholder="Search a company by name"
                            inputClass="h-3rem" ref="cac"
                            field="companyName" style="z-index:10" name="eu_taxonomy_search_input"

                            @item-select="filter=false; singleton=true; table=false">
                <template #footer>
                  <ul v-if="responseArray && responseArray.length > 0" class="p-autocomplete-items pt-0">
                    <li class="p-autocomplete-item text-primary font-semibold" @click="filter=true; table=true;singleton=false; close();">View all ({{responseArray.length}}) results. </li>
                  </ul>
                </template>
              </AutoComplete>
          </span>
        </span>
      </div>
      <div class="col-2 text-left">
      </div>
    </div>
  </MarginWrapper>
  <template v-if="processed && table">
    <EuTaxoSearchResults :data="filter ? responseArray : [selectedCompany]" :processed="processed"/>
  </template>
  <template v-if="processed && singleton">
  <MarginWrapper >
    <div class="grid align-items-end">
      <div class="col-9">
        <CompanyInformation :companyID="selectedCompany.companyId"/>
      </div>
      <div class="col-3 pb-4 text-right">
        <Button label="Get Report" class="uppercase p-button">Get Report
          <i class="material-icons pl-3" aria-hidden="true">arrow_drop_down</i>
        </Button>
      </div>
    </div>
  </MarginWrapper>
  <MarginWrapper bgClass="surface-800">
    <TaxonomyData :companyID="selectedCompany.companyId"/>
  </MarginWrapper>

  </template>
</template>

<script>
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import AutoComplete from 'primevue/autocomplete';

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.ContactInformation
const dataStore = new DataStore(api.getCompanies, contactSchema)
import EuTaxoSearchResults from "@/components/ui/EuTaxoSearchResults";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import CompanyInformation from "@/components/pages/company/CompanyInformation";
import TaxonomyData from "@/components/pages/taxonomy/TaxonomyData";

export default {
  name: "EuTaxoSearchBar",
  components: {MarginWrapper, EuTaxoSearchResults, AutoComplete, TaxonomyData, CompanyInformation},
  data() {
    return {
      singleton: false,
      processed: false,
      table: false,
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
    close(){
      this.$refs.cac.hideOverlay()
    },
    async searchCompany(event) {
      try {
        this.processed = false
        this.loading = true
        this.responseArray = await dataStore.perform(event.query, "", false).then(response => {
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
        this.processed = true
      }
    }
  }
}
</script>