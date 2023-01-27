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
        </DataTable>
        <p v-else>
          The resource you requested does not exist yet. You can create it:
          <router-link to="/companies/upload">Create Data</router-link>
        </p>
      </template>
    </template>
  </Card>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import PrimeButton from "primevue/button";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { CompanyInformation } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "RetrieveCompany",
  components: { Card, PrimeButton, DataTable, Column, FormKit },
  setup: function () {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data: () => ({
    table: false,
    responseArray: null,
    filter: false,
    loading: false,
    model: {} as CompanyInformation,
    response: null,
    companyInformation: null,
    selectedCompany: null,
    filteredCompanies: null,
    filteredCompaniesBasic: null,
    additionalCompanies: null,
  }),
  methods: {
    /**
     * Uses the Dataland API to retrieve all companies matching the companyName in the Form or
     * all companies without restrictions if the all parameter is set to true.
     * The companies are stored in the response property of the component.
     *
     * @param all whether to retrieve all companies or just companies matching companyName
     */
    async getCompanyByName(all = false) {
      try {
        this.loading = false;
        if (all) {
          this.model.companyName = "";
        }
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
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
});
</script>
