<template>
  <div v-if="response">
    {{response.data}}

  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
const api = new EuTaxonomyDataControllerApi()
const dataStore = new DataStore(api.getDataSet)
export default {
  name: "CompanyEU",
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
    this.getCompanyEUDataset()
  },
  methods: {
    async getCompanyEUDataset() {
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