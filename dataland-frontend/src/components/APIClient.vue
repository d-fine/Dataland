<template>
  <div id="app" class="container">
    <div class="row">
      <div class="col s12 m12">

        <div class="card">
          <div class="card-title">ESG Data Search</div>
          <div class="card-content ">

            <div class="row">
              <div class="col m6">
                <button class="btn btn-sm btn-warning ml-2" @click="clearGetOutput">Clear</button>
              </div>
              <div class="col m6">
                <button class="btn btn-sm btn-primary" @click="getAllData">Get All Data</button>
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
              <p> Data: {{ getResultByID.status }} </p>
              <p> Name: {{ getResultByID.status }} </p>
              <p> Payload: {{ getResultByID.status }} </p>

            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>

import http from "./http-common";

export default {
  name: "APIClient",
  data() {
    return {
      getResult: null,
      getResultByID: null,
      getResultByName: null
    }
  },
  methods: {

    async getAllData() {
      this.getResultByID = null
      try {
        const res = await http.get("/data");
        this.getResult = {
          status: res.status + "-" + res.statusText,
          headers: res.headers,
          data: res.data,
        };
      } catch (err) {
        this.getResult = err.response?.data || err;
      }
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
    },
  }
}

</script>
