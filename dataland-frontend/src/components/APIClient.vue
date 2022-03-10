<template>
  <div class="container">
      <button class="btn btn-sm" @click="getSkyminderByName">Click </button>
  </div>
</template>
<script>

import {DataControllerApi} from "@/clients/backend";
export default {
  name: "APIClient",
  components: {
  },
  data() {
    return {
      dataStore: new DataControllerApi(),
      loading: false,
      data: null
    }
  },
  methods: {
    async getSkyminderByName() {
      this.loading = true
      this.data = await this.dataStore.getDataSkyminderRequest("DEU", "BMW")
      this.loading = false
    },

    async clearGetOutput() {
      this.data = null
    },
  }
}



</script>