<template>
  <div id="app" class="container">
    <div class="row">
      <div class="col s12 m12">

        <div class="card">
          <div class="card-title"><h3>ESG Data Search</h3></div>
          <div class="card-content ">
            <div class="row">
              <div class="col m6">
                <button class="btn btn-sm " @click="getAllData">Get All Data</button>
              </div>
              <div class="col m6">
                <button class="btn btn-sm " @click="clearGetOutput">Clear</button>
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
      get_id: null,
      get_name: null,
      getResult: null,
      getResultByID: null,
      getResultByName: null
    }
  },
  methods: {
    async getAllData() {
      const dataStore = new DataStore("http://localhost:8080")
      this.getResult = await dataStore.getAll()
      this.getResultByID = null

    },
    async getDataById() {
      this.getResult = null

      let id = this.get_id;
      if (id) {
        try {
          const res = await http.get(`/data/${id}`);
          // const result = {
          //   data: res.data,
          //   status: res.status,
          //   statusText: res.statusText,
          //   headers: res.headers,
          //   config: res.config,
          // };
          this.getResultByID = {
            status: res.status + "-" + res.statusText,
            headers: res.headers,
            data: res.data,
          };
        } catch (err) {
          this.getResultByID = err.response?.data || err;
        }
      }
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
