<template>
  <div id="app" class="container">
    <div class="row">
      <div class="col s12 m12">

        <div class="card">
          <div class="card-title"><h3>ESG Data Search</h3></div>
          <div class="card-content ">
            <div class="row">
              <div class="col m6">
                <button test-label="getAllDataLabel" class="btn btn-sm " @click="getAllData">Get All Data</button>
              </div>
              <div class="col m6">
                <button test-label="clearGetOutputLabel" class="btn btn-sm " @click="clearGetOutput">Clear</button>
              </div>
            </div>
            <div class="row">
              <div class="input-field col s12 m6">
                <input type="text" v-model="this.data.id" class="autocomplete" placeholder="Search by ID"/>
                <button class="btn btn-sm btn-primary" @click="getDataById">Get by Id</button>
              </div>
              <div class="input-field col s12 m6 ">
                <input type="text" v-model="this.data.name" class="autocomplete" placeholder="Search by Name"/>
                <button class="btn btn-sm btn-primary pulse" @click="getDataById">Get by Name</button>
              </div>
            </div>
            <div v-if="this.data.allResult" class="alert alert-secondary mt-2" role="alert">
              <pre>{{ this.data.allResult }}</pre>
              <p> {{ this.data.allResult.data }} </p>
              <p> Name: {{ this.data.allResult.data[0].name }} </p>
              <p> ID: {{ this.data.allResult.data[0].id }} </p>
              <p> {{ this.data.allResult["data"] }} </p>
              <p> {{ this.data.allResult.status }} </p>
            </div>
            <div v-if="this.data.filteredResult" class="alert alert-secondary mt-2" role="alert">
              <pre>{{ this.data.filteredResult }}</pre>
              <p> Status: {{ this.data.filteredResult.status }} </p>
              <p> Data: {{ this.data.filteredResult.data }} </p>
              <p> Name: {{ this.data.filteredResult.data.name }} </p>
              <p> Payload: {{ this.data.filteredResult.data.payload }} </p>
            </div>
          </div>
        </div>
      </div>
      <div v-if="this.data.allResult" class="col m12">
        <table id="getResultTable">
          <caption>Table of Results</caption>
          <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="dataset in this.data.allResult.data" :key="dataset.id">
            <td>{{dataset.id}}</td>
            <td>{{dataset.name}}</td>
          </tr>
          </tbody>
        </table>
      </div>
      <div v-if="this.data.filteredResult" class="col m12">
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
            <td>{{ this.data.id }}</td>
            <td>{{ this.data.filteredResult.data.name }}</td>
            <td>{{ this.data.filteredResult.data.payload }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<script>

import {DataStore} from "@/service/DataStore";
import {Data} from "@/model/Data"
export default {

  name: "APIClient",
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
