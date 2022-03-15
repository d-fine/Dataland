<template>
  <div v-if="response" class="container">
    <div class="row">
      <div class="col m9 s12">
        <ResultTable :headers='["Data ID", "Data Type"]' :data="response.data"/>

      </div>

    </div>

    <table>
      <caption><h4>Dataset for {{response.data.companyName}}</h4></caption>
      <thead>
      <tr>
        <th  v-for="(header, i) in ['Data ID', 'Data Type', 'Link']" :key="i">{{ header }}</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(dataset, index) in response.data" :key="index">
        <td v-for="(item, i) in dataset" :key="i">
          {{item}}
        </td>
        <td> <router-link to="/">Company Information</router-link> {{}}</td>
      </tr>

      </tbody>
    </table>
    {{response.data}}


  </div>
</template>

<script>
import {CompanyDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
import ResultTable from "@/components/ui/ResultTable";
const api = new CompanyDataControllerApi()
const dataStore = new DataStore(api.getCompanyDataSets)
export default {
  name: "CompanyInformation",
  components: {ResultTable},
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