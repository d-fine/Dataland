<template>
  <div class="grid align-items-center">
    <div class="col-8 text-left">
      <h1 class="pl-3">Search EU Taxonomy data</h1>

    </div>
    <div class="col-4 text-right font-semibold">
      <span class="pr-3">Welcome to Dataland, Roger</span>
    </div>

  </div>
  <div class="grid">
    <div class="col-8 pl-3">
      <span class="p-fluid">
        <span class="p-input-icon-left ">
            <i class="pi pi-search"/>
            <InputText type="text" v-model="data" placeholder="Search by company name, CIN or ISIN"/>
        </span>
      </span>
    </div>
    <div class="col-2 text-left">

    <Button @click="getCompanyByName(true)" label="Search" class="uppercase" ><i class="pi pi-search pr-2"/>Search</Button>
    </div>
  </div>
  <div class="grid surface-800 align-items-center" v-if="action">
    <div class="col-1 text-left ml-8">
      <h2>Results</h2>
    </div>
    <div class="col-3 col-offset-7 surface-0 p-card " style="border-radius: 0.2rem">
      <div class="grid align-items-center">
        <div class="col-6 text-left">
        <span class="font-semibold">Avg Green asset ratio </span>
        </div>
        <div class="col-6 text-right text-green-500">
        <span class="font-semibold text-xl">78</span> <span> % </span>
        </div>
      </div>
    </div>
    <div class="col-12 text-left  pl-8 pr-8" >
      <template v-if="action">
        <DataTable  v-if="response" :value="response.data" responsive-layout="scroll">
          <Column field="companyName" header="COMPANY" :sortable="true" class="surface-0" >
            <template #body="{data}">
              <router-link :to="/companies/ + data.companyId" class="text-primary font-bold">{{ data.companyName }} </router-link>
            </template>
          </Column>
          <Column field="companyId" header="Company ID" :sortable="true" class="surface-0"> </Column>

        </DataTable>
        <p v-else>The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
      </template>
    </div>
  </div>
</template>

<script>
import InputText from 'primevue/inputtext';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';


import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.getCompaniesByName, contactSchema)

export default {
  name: "SearchTaxonomy",
  components: {InputText, DataTable, Column, Button},
  data() {
    return {
      data: null,
      response: null,
      action: false
    }
  },
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.action = false
        if (all) {
          this.data = ""
        }
        this.response = await dataStore.perform(this.data)

      } catch (error) {
        console.error(error)
        this.response = null
      } finally {
        this.action = true
      }
    }
  }
}
</script>

<style scoped>

</style>