<template>
  <div class="container">
    <div class="row">
      <div class="col s12 m12">
        <div class="card">
          <div class="card-title"><h2>Contact Data Search</h2>
          </div>
          <div class="card-content ">

            <div class="row">
              <div class="input-field col s12 m6">
                <input type="text" v-model="countryCode" class="autocomplete" id="countryCode" @keyup.enter="getSkyminderByName"/>
                <label for="countryCode">Please insert a 3 Letter country code</label>
              </div>
              <div class="input-field col s12 m6">
                <input type="text" v-model="companyName" class="autocomplete" id="companyName" @keyup.enter="getSkyminderByName"/>
                <label for="companyName">Please insert the name of the company</label>
              </div>
            </div>
            <div class="row">
              <div class="col m6">
                <button class="btn btn-sm" @click="clearGetOutput">Clear</button>
              </div>
              <div class="col m6">
                <button class="btn btn-sm" @click="getSkyminderByName">Get Skyminder by Name</button>
              </div>
            </div>

            <div class="progress" v-if="loading">
              <div class="indeterminate" ></div>
            </div>

          </div>
        </div>
      </div>
      <div v-if="response" class="col m12">
        <ResultTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone', 'Identifier']" :data="response.data"/>
      </div>

    </div>
  </div>
</template>
<script>

import {DataControllerApi} from "@/clients/backend";
import ResultTable from "@/components/ui/ResultTable";

export default {
  name: "APIClient",
  components: {
    ResultTable
  },
  data() {
    return {
      dataStore: new DataControllerApi(),
      loading: false,
      response: null,
      countryCode: null,
      companyName: null
    }
  },
  methods: {
    async getSkyminderByName() {
      this.loading = true
      try {
        this.response = await this.dataStore.getDataSkyminderRequest(this.countryCode, this.companyName)
      } catch (error) {
        console.error(error)
      }
      this.loading = false
    },

    async clearGetOutput() {
      this.countryCode = null
      this.companyName = null
    },
  }
}



</script>