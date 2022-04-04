<template>
  <Card class="col-12">
    <template #title>
      Company Search
    </template>
    <template #content>
      <FormKit
          v-model="data"
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
      <template v-if="action">
        <DataTable v-if="response" :value="response.data" responsive-layout="scroll">
          <Column field="companyInformation.companyName" header="COMPANY" :sortable="true" class="surface-0" >
          </Column>
          <Column field="companyInformation.industrialSector" header="SECTOR" :sortable="true" class="surface-0"> </Column>
          <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0"> </Column>
          <Column field="companyId" header="" class="surface-0"> <template #body="{data}">
            <router-link :to="'/companies/' + data.companyId + '/eutaxonomies'" class="text-primary no-underline font-bold"> <span> VIEW</span> <span class="ml-3">></span></router-link>
          </template> </Column>
        </DataTable>
        <p v-else>The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
      </template>
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

export default {
  name: "RetrieveCompany",
  components: {Card, Button, DataTable, Column , FormKit},

  data: () => ({
    data: {},
    model: {},
    response: null,
    companyInformation: null,
    response_error: false,
    action: false
  }),
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.action = false
        if (all) {
          this.data.companyName = ""
        }
        this.response = await dataStore.perform(this.data.companyName)
        console.log(this.response.data[0].companyInformation)

      } catch (error) {
        console.error(error)
        this.response = null
      } finally {
        this.action = true
      }
    }
  },
}
</script>