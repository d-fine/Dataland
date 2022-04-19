<template>
  <MarginWrapper bgClass="surface-800" v-if="showIndexPanel">
    <div class="grid">
      <div class="col-12 text-left">
        <h4 class="mb-0">Choose by stock market index</h4>
      </div>
      <div class="col-3" v-for="(stockIndex, index) in Object.keys(stockIndexObject)" :key="index"
           @click="handleIndexCardClick(stockIndex, index)">
        <IndexCard :stockIndex="stockIndex"/>
      </div>
    </div>
  </MarginWrapper>
</template>

<script>


import IndexCard from "@/components/pages/indices/IndexCard";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "IndexPanel",
  components: {IndexCard, MarginWrapper},
  props: ['stockIndexObject', 'showIndexPanel'],
  methods: {
    handleIndexCardClick(stockIndex, index) {
      this.$emit("index-click", stockIndex, index)
    }
  }
}
</script>

<style scoped>

</style>