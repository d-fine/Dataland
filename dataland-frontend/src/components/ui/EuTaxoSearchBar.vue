<template>
  <MarginWrapper>
    <pre>{{ selectedCompany }}</pre>
    <p>

      Query: {{ route.query }}
      Selected Company: {{ selectedCompany }}
      Presence: {{ presence }}
    </p>
    <div class="grid">
      <div class="col-8 text-left">
        <span class="p-fluid">
          <span class="p-input-icon-left p-input-icon-right ">
              <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                    <i v-if="loading" class="pi pi-spinner spin" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                    <i v-else aria-hidden="true"/>
              <AutoComplete v-model="selectedCompany" :suggestions="filteredCompaniesBasic" @focus="focused" @focusout="focus=false"
                            @complete="searchCompany($event)" placeholder="Search a company by name"
                            inputClass="h-3rem" ref="cac"
                            field="companyName" style="z-index:10" name="eu_taxonomy_search_input"
                            @keyup.enter="filter=true; table=true; $router.push({name: 'Search Eu Taxonomy', query: {input: selectedCompany}}); queryCompany(); close();"

                            @item-select="filter=false; singleton=true; table=false; $router.push(`/companies/${selectedCompany.companyId}/eutaxonomies`)">
                <template #footer>
                  <ul v-if="responseArray && responseArray.length > 0" class="p-autocomplete-items pt-0">
                    <li class="p-autocomplete-item text-primary font-semibold"
                        @click="filter=true; table=true;singleton=false; close(); $router.push({name: 'Search Eu Taxonomy', query: {input: selectedCompany}})">View all ({{ responseArray.length }}) results. </li>
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
    <EuTaxoSearchResults :data="responseArray" :processed="processed"/>
  </template>
  <template v-if="processed && singleton">
    <MarginWrapper>
      <div class="grid align-items-end">
        <div class="col-9">
          <CompanyInformation :companyID="parseInt(selectedCompany.companyId)"/>
        </div>
        <div class="col-3 pb-4 text-right">
          <Button label="Get Report" class="uppercase p-button">Get Report
            <i class="material-icons pl-3" aria-hidden="true">arrow_drop_down</i>
          </Button>
        </div>
      </div>
    </MarginWrapper>
    <MarginWrapper bgClass="surface-800">
      <TaxonomyData :companyID="parseInt(selectedCompany.companyId)"/>
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
import Button from "primevue/button";
import {useRoute} from "vue-router"

export default {
  name: "EuTaxoSearchBar",
  components: {MarginWrapper, EuTaxoSearchResults, AutoComplete, TaxonomyData, CompanyInformation, Button},
  data() {
    return {
      focus: false,
      presence: "No",
      route: useRoute(),
      singleton: false,
      processed: false,
      table: false,
      responseArray: this.paramsArray,
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
  props: {
    paramsSelection: {
      type: String,
      default: ""
    },
    paramsArray: {
      type: Array,
      default() {
        return [
          {}
        ]
      }
    }
  },
  mounted() {
    if (this.route.query.input) {
      this.selectedCompany = this.route.query.input
      this.queryCompany()
    }
  },
  methods: {
    focused(){
      this.$emit('focus', "hallo")
    },
    responseMapper(response){
      return response.data.map(e => ({
        "companyName": e.companyInformation.companyName,
        "companyInformation": e.companyInformation,
        "companyId": e.companyId
      }))
    },
    close() {
      this.$refs.cac.hideOverlay()
    },
    async searchCompany(event) {
      try {
        this.processed = false
        this.loading = true
        this.responseArray = await dataStore.perform(event.query, "", true).then(this.responseMapper)
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
        this.additionalCompanies = this.responseArray.slice(0)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.processed = true
      }
    },
    async queryCompany() {
      try {
        this.processed = false
        this.loading = true
        this.responseArray = await dataStore.perform(this.selectedCompany, "", false).then(this.responseMapper)
        this.filteredCompaniesBasic = this.responseArray.slice(0, 3)
        this.additionalCompanies = this.responseArray.slice(0)
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false
        this.processed = true
        this.table = true
      }
    }
  }
}
</script>