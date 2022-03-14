<template>
  <div class="container">
    <div class="row">
      <div class="col m12 s12">
        <div class="card">
          <div class="card-title"><h2>Skyminder Data Search</h2>
          </div>
          <div class="card-content ">
            <FormKit v-model="data" type="form" @submit="getSkyminderByName">
              <FormKitSchema
                  :data="data"
                  :schema="schema"
              />
            </FormKit>
            <div class="progress" v-if="loading">
              <div class="indeterminate" ></div>
            </div>
            <div v-if="response" class="col m12">
              <ResultTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']" :data="response.data"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {FormKit, FormKitSchema} from "@formkit/vue";
import {SkyminderControllerApi} from "@/clients/backend";

import DataStore from "@/services/DataStore";

const api = new SkyminderControllerApi()
const dataStore = new DataStore(api.getDataSkyminderRequest)
import ResultTable from "@/components/ui/ResultTable";
export default {
  name: "RetrieveSkyminder",
  components: {FormKitSchema, FormKit, ResultTable},

  data: () => ({
    data: {},
    schema: dataStore.getSchema(),
    model: {},
    response: null,
    loading: false
  }),
  methods: {
    async getSkyminderByName() {
      try {
        // ToDo: auto data.*
        const inputArgs = Object.values(this.data)
        inputArgs.splice(0, 1)
        this.loading = true
        this.response = await dataStore.perform(...inputArgs, {baseURL: process.env.VUE_APP_API_URL})
        this.loading = false
        // ToDO: Results Table
      } catch (error) {
        console.error(error)
      }
    }
  },

}

</script>

<style>
@import "../../assets/css/buttons.css";
@import "../../assets/css/forms.css";
</style>