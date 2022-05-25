<template>
  <div></div>
</template>

<script>
import {MetaDataControllerApi} from "../../../../build/clients/backend/api";
import {axiosDefaultConfiguration} from "@/services/AxiosDefaultConfiguration"

const metaDataControllerApi = new MetaDataControllerApi(axiosDefaultConfiguration)

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
  created() {
    this.getData()
  },
  methods: {
    async getData() {
      this.response = await metaDataControllerApi.getListOfDataMetaInfo("")
    }
  }
}
</script>
