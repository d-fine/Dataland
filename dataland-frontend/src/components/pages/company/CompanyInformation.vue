<template>
  <div v-if="companyInfo" class="grid align-items-end text-left">
    <div class="col-4">
      <h1>{{companyInfo.data.companyName}}</h1>
    </div>
    <div class="col-4 mb-4">
      <span>Market Cap:</span> <span class="font-semibold">$45.00 B (Placeholder)</span>
    </div>
    <div class="col-4 mb-4">
      Company Reports:
    </div>
    <div class="col-4">
      <span>Sector: </span> <span class="font-semibold" >Manufacturing (Placeholder)</span>
    </div>
    <div class="col-4">
      <span>Headquarter: </span> <span class="font-semibold" >Herzogenaurach (Placeholder)</span>
    </div>
    <div class="col-4">
      <Button label="Financial and sustainability" class="uppercase bg-white text-primary font-semibold border-2"> Financial and sustainability 2021 <i class="pi pi-download pl-2" aria-hidden="true"/> </Button>
    </div>
  </div>
</template>

<script>
import {CompanyDataControllerApi} from "@/../build/clients/backend";
import Button from "primevue/button";
import {DataStore} from "@/services/DataStore";
const companyApi = new CompanyDataControllerApi()
const companyStore = new DataStore(companyApi.getCompanyById)
export default {
  name: "CompanyInformation",
  components: { Button},
  data() {
    return {
      response: null,
      company: null,
      companyInformation: null
    }
  },
  props: {
    companyID: {
      type: Number
    }
  },
  created() {
      this.getCompanyInformation()
  },
  watch: {
    companyID(){
      this.getCompanyInformation()
    }
  },
  methods: {
    async getCompanyInformation() {
      this.companyInfo = await companyStore.perform(this.companyID)
      this.company = await companyStore.perform(this.companyID)
      this.companyInformation = this.company.data.companyInformation
      console.log(this.companyInformation)
      console.log(this.companyInformation.companyName)
      console.log(this.company.data.companyId)
    },
    async getCompanyDataset() {
      this.response = await dataStore.perform(this.companyID, "")
    }
  }
}
</script>