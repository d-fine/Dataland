<template>
  <div class="grid">
    <div class="col-8 pl-3">
      <span class="p-fluid">
        <span class="p-input-icon-left ">
            <i class="pi pi-search" aria-hidden="true"/>
            <InputText type="text" v-model="model" placeholder="Search by company name, CIN or ISIN" disabled="true"/>
        </span>
      </span>
    </div>
    <div class="col-2 text-left">
      <Button @click="getCompanyByName(true)" label="Search" class="uppercase" ><i class="pi pi-search pr-2" aria-hidden="true"/>Search</Button>
    </div>
  </div>
  <EuTaxoSearchResults v-if="action" :data="response.data" :action="action" />

</template>

<script>
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import {DataStore} from "@/services/DataStore";
import backend from "@/../build/clients/backend/backendOpenApi.json";
const api = new CompanyDataControllerApi()
const contactSchema = backend.components.schemas.PostCompanyRequestBody
const dataStore = new DataStore(api.getCompaniesByName, contactSchema)


import InputText from "primevue/inputtext";
import Button from "primevue/button";
import EuTaxoSearchResults from "@/components/ui/EuTaxoSearchResults";

export default {
  name: "EuTaxoSearchBar",
  components: {EuTaxoSearchResults, InputText, Button},
  data() {
    return {
      model: null,
      response: null,
      action: false
    }
  },
  methods: {
    async getCompanyByName(all = false) {
      try {
        this.action = false
        if (all) {
          this.model = ""
        }
        this.response = await dataStore.perform(this.model)

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