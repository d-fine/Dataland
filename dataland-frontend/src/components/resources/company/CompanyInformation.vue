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

  </div>
</template>

<script>

import {getCompanyDataControllerApi} from "@/services/ApiClients"
import {numberFormatter} from "@/utils/currencyMagnitude";

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
        this.company = await getCompanyDataControllerApi().getCompanyById(this.companyID)
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