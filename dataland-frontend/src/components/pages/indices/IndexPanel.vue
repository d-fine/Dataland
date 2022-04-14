<template>

  <div class="grid">
    <div class="col-12 text-left">
    <h4 class="mb-0">Choose by stock market index</h4>
    </div>
    <div class="col-3" v-for="(stockIndex, index) in Object.keys(stockIndexObject)" :key="index" @click="handleIndexCardClick(stockIndex, index)">
      <IndexCard  :stockIndex="stockIndex"/>
    </div>
  </div>
</template>

<script>


import IndexCard from "@/components/pages/indices/IndexCard";

export default {
  name: "IndexPanel",
  components: {IndexCard},
  props: ['stockIndexObject'],
  methods: {
    handleIndexCardClick(stockIndex, index){
      this.$emit("index-click", stockIndex, index)
    }
  }
}
</script>

<style scoped>

</style>