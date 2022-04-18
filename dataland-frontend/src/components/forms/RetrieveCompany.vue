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
      <Button @click="getCompanyByName(true)" label="Show all companies" name="show_all_companies_button" />
      <br>
      <template v-if="loading">
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
        <div class="col-10 text-left">
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
          <p>Selection: {{ selectedCompany }}</p>
          <p v-if="filter">Filter: {{ filteredCompaniesBasic }} </p>
          <DataTable v-if="table" :value="filter ? responseArray : [selectedCompany] "  responsive-layout="scroll" paginator="true" :alwaysShowPaginator="false" rows="5">
            <Column field="companyInformation.companyName" header="COMPANY" :sortable="true" class="surface-0" >
            </Column>
            <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="surface-0"> </Column>
            <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0"> </Column>
            <Column field="companyId" header="" class="surface-0"> <template #body="{data}">
              <router-link :to="'/companies/' + data.companyId + '/eutaxonomies'" class="text-primary no-underline font-bold"> <span> VIEW</span> <span class="ml-3">></span></router-link>
            </template> </Column>
          </DataTable>
        </div>
        <div class="col-2" v-if="loading">
          <ProgressSpinner/>
        </div>
        <pre>Object: {{stockIndexobject}}</pre>
        <p>GS: {{stockIndexobject.GeneralStandards}}</p>
      </div>
    </template>
  </Card>
</template>

<script>
import {FormKit} from "@formkit/vue";
import {CompanyDataControllerApi} from "@/../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"

const companyDataControllerApi = new CompanyDataControllerApi()
const dataStore = new ApiWrapper(companyDataControllerApi.getCompanies)
import Card from 'primevue/card';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import AutoComplete from 'primevue/autocomplete';
import ProgressSpinner from 'primevue/progressspinner';
import {stockIndexObject} from '@/utils/indexMapper'

export default {
  name: "RetrieveCompany",
  components: {Card, Button, DataTable, Column, FormKit, AutoComplete, ProgressSpinner},

  data: () => ({
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
    additionalCompanies: null,
    stockIndexobject: stockIndexObject()
  }),
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.loading = false
        if (all) {
          this.model.companyName = ""
        }
        this.response = await dataStore.perform(this.model.companyName, "", true)
      } catch (error) {
        console.error(error)
        this.response = null
      } finally {
        this.loading = true
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
  },
}
</script>