<template>
  <div v-if="response">
    {{response.data}}

  </div>
</template>

<script>
import {CompanyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompanyDataSets)
export default {
  name: "CompanyInformation",
  components: {},
  data() {
    return {
      response: null
    }
  },
  props:{
    companyID: {
      default: 1,
      type: Number
    }
  },
  created() {
    this.getCompanyDataset()
  },
  methods: {
    async getCompanyDataset() {
      try {
        this.response = await dataStore.perform(this.companyID, {baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response)
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>

<style scoped>

</style>