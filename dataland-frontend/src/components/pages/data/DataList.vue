<template>
  <div></div>
</template>

<script>
import {MetaDataControllerApi} from "@/clients/backend";
import {DataStore} from "@/services/DataStore";
const api = new MetaDataControllerApi()
const dataStore = new DataStore(api.getListOfDataMetaInfo())

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
      this.response = await dataStore.perform([""])
    }
  }
}
</script>

<style scoped>

</style>