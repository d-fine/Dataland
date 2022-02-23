<template>
  <div id="app" class="container">
    <div class="card">
      <div class="card-header">Vue Axios GET - BezKoder.com</div>
      <div class="card-body">
        <div class="input-group input-group-sm">
          <button class="btn btn-sm btn-primary" @click="getAllData">Get All Data</button>

          <button class="btn btn-sm btn-warning ml-2" @click="clearGetOutput">Clear</button>
        </div>

        <div v-if="getResult" class="alert alert-secondary mt-2" role="alert">
          <pre>{{getResult}}</pre>
        <p> {{getResult.data}} </p>
        <p> Name: {{getResult.data[0].name}} </p>
        <p> ID: {{getResult.data[0].id}} </p>
        <p> {{getResult["data"]}} </p>
        <p> {{getResult.status}} </p>
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
    }
  },
  methods: {

    async getAllData() {
      try {
        const res = await http.get("/data");
        const result = {
          status: res.status + "-" + res.statusText,
          headers: res.headers,
          data: res.data,
        };
        this.getResult = result;
      } catch (err) {
        this.getResult = this.fortmatResponse(err.response?.data) || err;
      }
    },
    clearGetOutput() {
      this.getResult = null;
    },
  }
}

</script>
