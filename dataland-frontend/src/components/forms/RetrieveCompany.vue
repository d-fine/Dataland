<template>
  <Card class="col-12">
    <template #title> Company Search </template>
    <template #content>
      <FormKit
        v-model="model"
        :submit-attrs="{
          name: 'getCompanies',
        }"
        submit-label="Search Company"
        type="form"
        @submit="getCompanyByName()"
      >
        <FormKit
          type="text"
          name="companyName"
          validation="required"
          validation-visibility="submit"
          label="Company Name"
        />
      </FormKit>
      <PrimeButton @click="getCompanyByName(true)" label="Show all companies" name="show_all_companies_button" />
      <br />
      <template v-if="loading">
        <DataTable v-if="response" :value="response.data" responsive-layout="scroll">
          <Column field="companyInformation.companyName" header="COMPANY" :sortable="true" class="surface-0"> </Column>
          <Column field="companyInformation.sector" header="SECTOR" :sortable="true" class="surface-0"> </Column>
          <Column field="companyInformation.marketCap" header="MARKET CAP" :sortable="true" class="surface-0"> </Column>
          <Column field="companyId" header="" class="surface-0">
            <template #body="{ data }">
              <router-link
                :to="'/companies/' + data.companyId + '/eutaxonomies'"
                class="text-primary no-underline font-bold"
              >
                <span> VIEW</span> <span class="ml-3">></span></router-link
              >
            </template>
          </Column>
        </DataTable>
        <p v-else>
          The resource you requested does not exist yet. You can create it:
          <router-link to="/upload">Create Data</router-link>
        </p>
      </template>
    </template>
  </Card>
</template>

<script>
import { FormKit } from "@formkit/vue";
import { ApiClientProvider } from "@/services/ApiClients";

import Card from "primevue/card";
import PrimeButton from "primevue/button";
import DataTable from "primevue/datatable";
import Column from "primevue/column";

export default {
  name: "RetrieveCompany",
  components: { Card, PrimeButton, DataTable, Column, FormKit },

  data: () => ({
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
    additionalCompanies: null,
  }),
  inject: ["getKeycloakInitPromise"],
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.loading = false;
        if (all) {
          this.model.companyName = "";
        }
        const companyDataControllerApi = await new ApiClientProvider(
          this.getKeycloakInitPromise()
        ).getCompanyDataControllerApi();
        this.response = await companyDataControllerApi.getCompanies(this.model.companyName, "", true);
      } catch (error) {
        console.error(error);
        this.response = null;
      } finally {
        this.loading = true;
      }
    },
  },
};
</script>
