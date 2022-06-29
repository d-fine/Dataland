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
import {ApiClientProvider} from "@/services/ApiClients"
import DataTable from "primevue/datatable";

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
      const euTaxonomyDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getEuTaxonomyDataControllerApi()
      this.response = await euTaxonomyDataControllerApi.getData()
    }
  }
}
</script>
