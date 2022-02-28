<template>
  <div id="app" class="container">
    <div class="row">
      <div class="col s12 m12">
        <div class="card">
          <div class="card-title"><h3>ESG Data Search</h3>
          </div>
          <div class="card-content ">
            <div class="row">
              <div class="col m6">
              </div>
              <div class="col m6">
                <button class="btn btn-sm" @click="clearGetOutput">Clear</button>
              </div>
            </div>

            <div class="row">
              <div class="input-field col s12 m6">
                <input type="text" v-model="data.code" class="autocomplete" id="searchByIdCode"/>
                <label for="searchByIdCode">Please insert a 3 Letter country code</label>
              </div>
              <div class="input-field col s12 m6">
                <input type="text" v-model="data.name" class="autocomplete" id="searchByNameInput"/>
                <label for="searchByNameInput">Please insert the name of the company</label>
                <button class="btn btn-sm" @click="getSkyminderByName">Get Skyminder by Name</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="data.result" class="col m12">
        <ResultTable :headers="['Name', 'Address', 'Website', 'Email', 'Phone']" :data="data.result"/>
      </div>
    </div>
  </div>
</template>
<script>

import {DataStore} from "@/service/DataStore";
import {Data} from "@/model/Data"
import ResultTable from "@/components/ui/ResultTable"
export default {
  name: "APIClient",
  components: {
    ResultTable
  },
  data() {
    return {
      dataStore: new DataStore("http://localhost:8080/"),
      data: new Data()
    }
  },
  methods: {

    async getSkyminderByName() {
      this.data.getResult(await this.dataStore.getByName(this.data.code, this.data.name))
    },

    clearGetOutput() {
      this.data.clearAll()
    },
  }
}

</script>