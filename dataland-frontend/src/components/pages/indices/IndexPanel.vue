<template>

  <div class="grid">
    <div class="col-12 text-left">
    <h4 class="mb-0">Choose by stock market index</h4>

    </div>
    <div class="col-3" v-for="index in indices" :key="index">
      <IndexCard  :index="humanizeIndex(index)"/>
    </div>
  </div>
</template>

<script>


import IndexCard from "@/components/pages/indices/IndexCard";
import backend from "@/../build/clients/backend/backendOpenApi.json";
import {humanizeString} from "@/utils/stringHumanizer";

export default {
  name: "IndexPanel",
  components: {IndexCard},
  data() {
    return {indices: backend.components.schemas.CompanyInformation.properties["indices"].items.enum}
  },
  methods: {
    humanizeIndex(index){
      return humanizeString(index)
    }
  }
}
</script>

<style scoped>

</style>