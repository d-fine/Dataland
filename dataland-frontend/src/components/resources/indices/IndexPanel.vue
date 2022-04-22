<template>
  <MarginWrapper bgClass="surface-800" v-if="showIndexPanel">
    <div class="grid">
      <div class="col-12 text-left">
        <h4 class="mb-0">Choose by stock market index</h4>
      </div>
      <div class="col-3" v-for="(stockIndex, index) in stockIndices" :key="index"
           @click="handleIndexCardClick(stockIndex, index)">
        <IndexCard :index="index" :stockIndices="stockIndices"/>
      </div>
    </div>
  </MarginWrapper>
</template>

<script>


import IndexCard from "@/components/resources/indices/IndexCard";
import MarginWrapper from "@/components/wrapper/MarginWrapper";

export default {
  name: "IndexPanel",
  components: {IndexCard, MarginWrapper},
  props: ['stockIndices', 'showIndexPanel'],
  methods: {
    handleIndexCardClick(stockIndex, index) {
      this.$emit("index-click", stockIndex, index)
    }
  }
}
</script>