<template>
  <div id="app" class="container">
    <div class="card">
      <div class="card-header">Vue Axios GET - BezKoder.com</div>
      <div class="card-body">
        <div class="input-group input-group-sm">
          <button class="btn btn-sm btn-primary" @click="getAllData">Get All</button>
          <input type="text" ref="get_id" class="form-control ml-2" placeholder="Id" />
          <div class="input-group-append">
            <button class="btn btn-sm btn-primary" @click="getDataById">Get by Id</button>
          </div>
          <input type="text" ref="get_title" class="form-control ml-2" placeholder="Title" />
          <div class="input-group-append">
            <button class="btn btn-sm btn-primary" @click="getDataByTitle">Find By Title</button>
          </div>
          <button class="btn btn-sm btn-warning ml-2" @click="clearGetOutput">Clear</button>
        </div>

        <div v-if="getResult" class="alert alert-secondary mt-2" role="alert"><pre>{{getResult}}</pre></div>
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
    fortmatResponse(res) {
      return JSON.stringify(res, null, 2);
    },
    async getAllData() {
      try {
        const res = await http.get("/tutorials");
        const result = {
          status: res.status + "-" + res.statusText,
          headers: res.headers,
          data: res.data,
        };
        this.getResult = this.fortmatResponse(result);
      } catch (err) {
        this.getResult = this.fortmatResponse(err.response?.data) || err;
      }
    },
    async getDataById() {
      const id = this.$refs.get_id.value;
      if (id) {
        try {
          const res = await http.get(`/tutorials/${id}`);
          const result = {
            status: res.status + "-" + res.statusText,
            headers: res.headers,
            data: res.data,
          };
          // const result = {
          //   data: res.data,
          //   status: res.status,
          //   statusText: res.statusText,
          //   headers: res.headers,
          //   config: res.config,
          // };
          this.getResult = this.fortmatResponse(result);
        } catch (err) {
          this.getResult = this.fortmatResponse(err.response?.data) || err;
        }
      }
    },
    async getDataByTitle() {
      const title = this.$refs.get_title.value;
      if (title) {
        try {
          // const res = await instance.get(`/tutorials?title=${title}`);
          const res = await http.get("/tutorials", {
            params: {
              title: title,
            },
          });
          const result = {
            status: res.status + "-" + res.statusText,
            headers: res.headers,
            data: res.data,
          };
          this.getResult = this.fortmatResponse(result);
        } catch (err) {
          this.getResult = this.fortmatResponse(err.response?.data) || err;
        }
      }
    },
    clearGetOutput() {
      this.getResult = null;
    },
  }
}

</script>

<style scoped>

</style>