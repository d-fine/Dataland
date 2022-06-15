<template>
  <div></div>
</template>

<script>
import {ApiClientProvider} from "@/services/ApiClients"

export default {
  name: "DataList",
  components: {},
  data() {
    return {
      response: null,
      companyInfo: null
    }
  },
  props:{
    dataID: {
      default: 1,
      type: Number
    }
  },
  inject: ['getKeycloakInitPromise','keycloak_init'],
  created() {
    this.getData()
  },
  methods: {
    async getData() {
      const metaDataControllerApi = await new ApiClientProvider(this.getKeycloakInitPromise(), this.keycloak_init).getMetaDataControllerApi()
      this.response = await metaDataControllerApi.getListOfDataMetaInfo("")
    }
  }
}
</script>
