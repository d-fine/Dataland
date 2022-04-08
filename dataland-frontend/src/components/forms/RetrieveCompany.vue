<template>
  <Card class="col-12">
    <template #title>
      Company Search
    </template>
    <template #content>
      <FormKit
          v-model="model"
          :submit-attrs="{
                  'name': 'getCompanies'
                }"
          submit-label="Search Company"
          type="form"
          @submit="getCompanyByName()">
        <FormKit
            type="text"
            name="companyName"
            validation="required"
            validation-visibility="submit"
            label="Company Name"
        />

      </FormKit>
      <Button @click="getCompanyByName(true)" label="Show all companies" />
      <br>
      <template v-if="processed">
        <DataTable v-if="response" :value="response.data" responsive-layout="scroll">
          <Column field="companyInformation.companyName" header="COMPANY" :sortable="true" class="surface-0" >
          </Column>
          <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="surface-0"> </Column>
          <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0"> </Column>
          <Column field="companyId" header="" class="surface-0"> <template #body="{data}">
            <router-link :to="'/companies/' + data.companyId + '/eutaxonomies'" class="text-primary no-underline font-bold"> <span> VIEW</span> <span class="ml-3">></span></router-link>
          </template> </Column>
        </DataTable>
        <p v-else>The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
      </template>
      <div class="grid align-items-top">
        <div class="col-10">
        <span class="p-fluid">
             <span class="p-input-icon-left p-input-icon-right ">
            <i class="pi pi-search" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                  <i v-if="processed" class="pi pi-spinner spin" aria-hidden="true" style="z-index:20; color:#958D7C"/>
                  <i v-else aria-hidden="true"/>
            <AutoComplete v-model="selectedCompany" :suggestions="filteredCompaniesBasic"
                          @complete="searchCompany($event)" placeholder="something" inputClass="something"
                          field="companyName" style="z-index:10"
                          completeOnFocus forceSelection
                          @keyup.enter="filter=true" @item-select="filter=false"/>
        </span>

        </span>
          <p>Selection: {{ selectedCompany }}</p>
          <p v-if="filter">Filter: {{ filteredCompaniesBasic }} </p>
          <p v-if="additionalCompanies" class="text-primary">View all {{ additionalCompanies.length }} companies</p>
        </div>
        <div class="col-2" v-if="processed">
          <ProgressSpinner/>
        </div>
      </div>
    </template>
  </Card>
</template>

<script>
import {FormKit} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";

const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompaniesByName)
import Card from 'primevue/card';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import AutoComplete from 'primevue/autocomplete';
import ProgressSpinner from 'primevue/progressspinner';

export default {
  name: "RetrieveCompany",
  components: {Card, Button, DataTable, Column, FormKit, AutoComplete, ProgressSpinner},

  data: () => ({
    responseArray: null,
    filter: false,
    processed: false,
    model: {},
    response: null,
    companyInformation: null,
    selectedCompany: null,
    filteredCompanies: null,
    filteredCompaniesBasic: null,
    additionalCompanies: null
  }),
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.processed = false
        if (all) {
          this.model.companyName = ""
        }
        this.response = await dataStore.perform(this.model.companyName)
      } catch (error) {
        console.error(error)
        this.response = null
      } finally {
        this.processed = true
      }
    },
    async searchCompany(event) {
      try {
        this.processed = true
        this.responseArray = await dataStore.perform(event.query).then(response => {
              return response.data.map(e => ({
                "companyName": e.companyInformation.companyName,
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
        this.processed = false
      }
    }
  },
}
</script>