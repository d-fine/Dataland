<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-8 text-left pt-0">
        <p v-if="responseArray && responseArray.length > 0" class="text-primary mt-0">Select company or <a class="font-semibold text-primary no-underline" @click="filter=true; table=true" href="#">View all ({{responseArray.length}}) results. </a></p>
        <p v-else class="mt-0"> &nbsp; </p>
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right ">
              <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                    <i v-if="loading" class="pi pi-spinner spin" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                    <i v-else aria-hidden="true"/>
              <AutoComplete v-model="selectedCompany" :suggestions="filteredCompaniesBasic"
                            @complete="searchCompany($event)" placeholder="Search a company by name"
                            inputClass="h-3rem"
                            field="companyName" style="z-index:10" name="eu_taxonomy_search_input"

                            @keyup.enter="filter=true; table=true" @item-select="filter=false; table=true"/>
          </span>
        </span>
      </div>
      <div class="col-2 text-left">
      </div>
    </div>
  </MarginWrapper>
  <div v-if="processed && table">
    <EuTaxoSearchResults :data="filter ? responseArray : [selectedCompany]" :processed="processed"/>
  </div>

</template>

<script>
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import AutoComplete from 'primevue/autocomplete';

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.getCompaniesByName, contactSchema)
import EuTaxoSearchResults from "@/components/ui/EuTaxoSearchResults";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "EuTaxoSearchBar",
  components: {MarginWrapper, EuTaxoSearchResults, AutoComplete},
  data() {
    return {
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
    async searchCompany(event) {
      try {
        this.processed = false
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
        this.processed = true
      }
    }
  }
}
</script>