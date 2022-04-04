<template>
  <Card class="col-5 col-offset-1">
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
      <DataTable  v-if="response" :value="response.data" stripedRows responsive-layout="scroll">
        <Column field="companyName" header="Company Name" :sortable="true" >
          <template #body="{data}">
            <router-link :to="/companies/ + data.companyId" class="text-primary font-bold">{{ data.companyName }} </router-link>
          </template>
        </Column>
        <Column field="companyId" header="Company ID" :sortable="true"> </Column>

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
import backend from "@/../build/clients/backend/backendOpenApi.json";

const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
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