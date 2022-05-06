<template>
  <div v-if="company" class="grid align-items-end text-left">
    <div class="col-12">
      <h1 class="mb-0">{{companyInformation.companyName}}</h1>
    </div>

    <div class="col-4">
      <span>Market Cap:</span> <span class="font-semibold">€ {{ orderOfMagnitudeSuffix(companyInformation.marketCap) }}</span>
    </div>
    <div class="col-4">
      <span>Headquarter: </span> <span class="font-semibold" >{{companyInformation.headquarters}}</span>
    </div>
    <div class="col-4">
      <span>Sector: </span> <span class="font-semibold" >{{companyInformation.sector}}</span>
    </div>

    <div class="col-4">
      Company Reports: <span class="cursor-pointer underline text-primary font-semibold">Financial Data 2021</span>
    </div>
    <div class="col-4">
      <span  class="cursor-pointer underline text-primary font-semibold">Sustainability Data 2021</span>
    </div>
  </div>
</template>

<script>

import {CompanyDataControllerApi} from "../../../../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"
import {numberFormatter} from "@/utils/currencyMagnitude";

const companyDataControllerApi = new CompanyDataControllerApi()
const getCompanyByIdWrapper = new ApiWrapper(companyDataControllerApi.getCompanyById)
export default {
  name: "CompanyInformation",
  data() {
    return {
      response: null,
      company: null,
      companyInformation: null
    }
  },
  props: {
    companyID: {
      type: String
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
        this.company = await getCompanyByIdWrapper.perform(this.companyID)
        this.companyInformation = this.company.data.companyInformation
      } catch (error) {
        console.error(error)
        this.company=null
      }

    },
     orderOfMagnitudeSuffix(value){
      return numberFormatter(value)
     }
  }
}
</script>