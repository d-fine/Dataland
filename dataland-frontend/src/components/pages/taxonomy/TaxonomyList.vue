<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div v-if="response">
          <DataTable>
            <caption><h4>Available datasets</h4></caption>
            <thead>
            <tr>
              <th v-for="(header, i) in ['Data ID', 'Data Type', 'Link']" :key="i" scope="col">{{ header }}</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(dataset, index) in response.data" :key="index">
              <td v-for="(item, i) in dataset.dataIdentifier" :key="i">
                {{ item }}
              </td>
              <td>
                <router-link :to='"/eutaxonomies/" + dataset.dataIdentifier["Data ID"]'>Data Information</router-link>
              </td>
            </tr>
            </tbody>
          </DataTable>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {EuTaxonomyDataControllerApi} from "@/../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"
import DataTable from "primevue/datatable";

const euTaxonomyDataControllerApi = new EuTaxonomyDataControllerApi()
const getDataWrapper = new ApiWrapper(euTaxonomyDataControllerApi.getData)
export default {
  name: "TaxonomyData",
  components: {DataTable},

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
      this.response = await getDataWrapper.perform()
    }
  }
}
</script>
