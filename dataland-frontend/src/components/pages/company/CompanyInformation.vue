<template>
  <div v-if="company" class="grid align-items-end text-left">
    <div class="col-4">
      <h1>{{companyInformation.companyName}}</h1>
    </div>
    <div class="col-4 mb-4">
      <span>Market Cap:</span> <span class="font-semibold">{{OMS(companyInformation.marketCap)}}</span>
    </div>
    <div class="col-4 mb-4">
      Company Reports:
    </div>
    <div class="col-4">
      <span>Sector: </span> <span class="font-semibold" >{{companyInformation.industrialSector}}</span>
    </div>
    <div class="col-4">
      <span>Headquarter: </span> <span class="font-semibold" >{{companyInformation.headquarters}}</span>
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
import {nFormatter} from "@/utils/currencyMagnitude";

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
      try {
        this.company = await companyStore.perform(this.companyID)
        this.companyInformation = this.company.data.companyInformation
      } catch (error) {
        console.error(error)
        this.company=null
      }

    },
     OMS(value){
      return nFormatter(value)
     }
  }
}
</script>