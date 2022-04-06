<template>
  <MarginWrapper>
    <div class="grid">
      <div class="col-8">
      <span class="p-fluid">
        <span class="p-input-icon-left ">
            <i class="pi pi-search" aria-hidden="true"/>
            <InputText type="text" v-model="model" placeholder="Search by company name, LEI, PermID or ISIN"
                       disabled="true" name="eu_taxonomy_search_input"/>
        </span>
      </span>
      </div>
      <div class="col-2 text-left">
        <Button @click="getCompanyByName(true)" label="Search" class="uppercase p-button-sm"><i
            class="material-icons pr-2" aria-hidden="true">search</i> Search
        </Button>
      </div>
    </div>
  </MarginWrapper>
  <div v-if="action && response">
    <EuTaxoSearchResults :data="response.data" :action="action"/>
  </div>
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
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "EuTaxoSearchBar",
  components: {MarginWrapper, EuTaxoSearchResults, InputText, Button},
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