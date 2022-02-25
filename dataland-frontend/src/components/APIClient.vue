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
                <button class="btn btn-sm" @click="getAllData" id="getAllData">Get All Data</button>
              </div>
              <div class="col m6">
                <button class="btn btn-sm" @click="clearGetOutput">Clear</button>
              </div>
            </div>
            <div class="row">
              <div class="input-field col s12 m6">
                <input type="text" v-model="data.id" class="autocomplete" id="searchByIdInput"/>
                <label for="searchByIdInput">Search by ID</label>
                <button class="btn btn-sm" @click="getDataById">Get by Id</button>
              </div>
              <div class="input-field col s12 m6 ">
                <input type="text" v-model="data.name" class="autocomplete" id="searchByNameInput"/>
                <label for="searchByNameInput">Search by Name</label>
                <button class="btn btn-sm" @click="getDataById">Get by Name</button>
              </div>
            </div>
            <div v-if="data.allResult" class="alert alert-secondary mt-2" role="alert">
              <pre>{{ data.allResult }}</pre>
              <p > {{ data.allResult.data }} </p>
              <p> Name: {{ data.allResult.data[0].name }} </p>
              <p id="resultsID"> ID: {{ data.allResult.data[0].id }} </p>
              <p> {{ data.allResult["data"] }} </p>
            </div>
            <div v-if="data.filteredResult" class="alert alert-secondary mt-2" role="alert">
              <pre>{{ data.filteredResult }}</pre>
              <p> Status: {{ data.filteredResult.status }} </p>
              <p> Data: {{ data.filteredResult.data }} </p>
              <p> Name: {{ data.filteredResult.data.name }} </p>
              <p> Payload: {{ data.filteredResult.data.payload }} </p>
            </div>
          </div>
        </div>
      </div>

      <div v-if="data.filteredResult" class="col m12">
        <table id="getResultByIDTable">
          <caption>Table of Results by ID</caption>
          <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Payload</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>{{ data.id }}</td>
            <td>{{ data.filteredResult.data.name }}</td>
            <td>{{ data.filteredResult.data.payload }}</td>
          </tr>
          </tbody>
        </table>
      </div>
      <div v-if="data.allResult" class="col m12">
        <ResultTable :headers="['ID', 'Name']" :data="data.allResult.data"/>
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
      dataStore: new DataStore("http://localhost:8080"),
      data: new Data()
    }
  },
  methods: {
    async getAllData() {
      this.data.getAllResult(await this.dataStore.getAll())
    },
    async getDataById() {
      this.data.getFilteredResult(await this.dataStore.getById(this.data.id))
    },
    clearGetOutput() {
      this.data.clearAll()
    },
  }
}

</script>