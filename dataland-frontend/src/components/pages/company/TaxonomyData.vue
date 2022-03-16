<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div v-if="response">
          <table>
            <caption><h4>Available datasets</h4></caption>
            <thead>
            <tr>
              <th  v-for="(header, i) in ['Data ID', 'Data Type', 'Link']" :key="i">{{ header }}</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(dataset, index) in response.data" :key="index">
              <td v-for="(item, i) in dataset.dataIdentifier" :key="i">
                {{item}}
              </td>
              <td> <router-link :to='"/eutaxonomies/" + dataset.dataIdentifier["Data ID"]'>Data Information</router-link> </td>
            </tr>

            </tbody>
          </table>
          <ResultTable v-if="response" :headers="['Name', 'ID', 'Link']" :data="response.data" entity="Company Search" route="/companies/"/>
          {{response}}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi} from "@/clients/backend";
import ResultTable from "@/components/ui/ResultTable";
import {DataStore} from "@/services/DataStore";
const api = new EuTaxonomyDataControllerApi()
const dataStore = new DataStore(api.getData)
export default {
  name: "TaxonomyData",
  components: {ResultTable},

  data() {
    return {
      response: null
    }
  },
  created() {
    this.getTaxoData()
  },
  methods: {
    async getTaxoData() {
      try {
        this.response = await dataStore.perform({baseURL: process.env.VUE_APP_API_URL})
        console.log(this.response)
      } catch (error) {
        console.error(error)
      }
    }
  }
}
</script>
