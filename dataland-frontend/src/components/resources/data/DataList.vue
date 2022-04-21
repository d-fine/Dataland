<template>
  <div></div>
</template>

<script>
import {MetaDataControllerApi} from "../../../../build/clients/backend/api";
import {ApiWrapper} from "@/services/ApiWrapper"

const metaDataControllerApi = new MetaDataControllerApi()
const getListOfDataMetaInfoWrapper = new ApiWrapper(metaDataControllerApi.getListOfDataMetaInfo)

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
      this.response = await getListOfDataMetaInfoWrapper.perform("")
    }
  }
}
</script>
