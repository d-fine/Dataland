<template>
  <div></div>
</template>

<script>
import {getMetaDataControllerApi} from "@/services/ApiClients"

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
      this.response = await getMetaDataControllerApi.getListOfDataMetaInfo("")
    }
  }
}
</script>
