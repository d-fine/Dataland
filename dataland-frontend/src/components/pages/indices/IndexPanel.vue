<template>

  <div class="grid">
    <div class="col-12 text-left">
    <h4 class="mb-0">Choose by stock market index</h4>
    </div>
    <div class="col-3" v-for="(item, index) in indexArray" :key="index" @click="handleIndexCardClick(item, index)">
      <IndexCard  :index="item"/>
    </div>
  </div>
</template>

<script>


import IndexCard from "@/components/pages/indices/IndexCard";
import backend from "@/../build/clients/backend/backendOpenApi.json";

export default {
  name: "IndexPanel",
  components: {IndexCard},
  props: ['indexArray'],
  data() {
    return {
      indices: backend.components.schemas.CompanyInformation.properties["indices"].items.enum,
    }
  },
  methods: {
    handleIndexCardClick(item, index){
      this.$router.push({name: 'Search Eu Taxonomy', query: {input: item}})
      this.$emit("index-click", index)
    }
  }
}
</script>

<style scoped>

</style>