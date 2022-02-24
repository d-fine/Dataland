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
                <input type="text" v-model="get_id" class="autocomplete" placeholder="Search by ID"/>
                <button class="btn btn-sm btn-primary" @click="getDataById">Get by Id</button>
              </div>
              <div class="input-field col s12 m6 ">
                <input type="text" v-model="get_name" class="autocomplete" placeholder="Search by Name"/>
                <button class="btn btn-sm btn-primary pulse" @click="getDataById">Get by Name</button>
              </div>
            </div>
            <div v-if="getResult" class="alert alert-secondary mt-2" role="alert">
              <pre>{{ getResult }}</pre>
              <p> {{ getResult.data }} </p>
              <p> Name: {{ getResult.data[0].name }} </p>
              <p> ID: {{ getResult.data[0].id }} </p>
              <p> {{ getResult["data"] }} </p>
              <p> {{ getResult.status }} </p>
            </div>
            <div v-if="getResultByID || getResultByName" class="alert alert-secondary mt-2" role="alert">
              <pre>{{ getResultByID }}</pre>
              <p> Status: {{ getResultByID.status }} </p>
              <p> Data: {{ getResultByID.data }} </p>
              <p> Name: {{ getResultByID.data.name }} </p>
              <p> Payload: {{ getResultByID.data.payload }} </p>
            </div>
          </div>
        </div>
      </div>
      <div v-if="getResult" class="col m12">
        <table id="getResultTable">
          <caption>Table of Results</caption>
          <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="dataset in getResult.data" :key="dataset.id">
            <td>{{dataset.id}}</td>
            <td>{{dataset.name}}</td>
          </tr>
          </tbody>
        </table>
      </div>
      <div v-if="getResultByID" class="col m12">
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
            <td>{{get_id}}</td>
            <td>{{getResultByID.data.name}}</td>
            <td>{{getResultByID.data.payload}}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<script>

import {DataStore} from "@/service/DataStore";
export default {

  name: "APIClient",
  data() {
    return {
      dataStore: new DataStore("http://localhost:8080"),
      get_id: null,
      get_name: null,
      getResult: null,
      getResultByID: null,
      getResultByName: null
    }
  },
  methods: {
    async getAllData() {
      this.getResultByID = null
      this.getResult = await this.dataStore.getAll()
    },
    async getDataById() {
      this.getResult = null
      this.getResultByID = await this.dataStore.getById(this.get_id)
    },
    clearGetOutput() {
      this.getResult = null;
      this.getResultByID = null;
      this.getResultByName = null;
      this.get_id = null;
      this.get_name = null;

    },
  }
}

</script>
