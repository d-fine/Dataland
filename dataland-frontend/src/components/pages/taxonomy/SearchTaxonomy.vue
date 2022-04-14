<template>
  <MarginWrapper>
    <div class="grid align-items-center">
      <div class="col-8 text-left">
        <h1 class="mb-0">Search EU Taxonomy data</h1>
      </div>
      <div class="col-4 text-right font-semibold">
        <span title="login message">Welcome to Dataland, Roger</span>
      </div>

    </div>
  </MarginWrapper>
    <EuTaxoSearchBar @autocomplete-focus="getAutoCompleteFocus" :stockIndexObject="stockIndexObject" ref="euTaxoSearchBar"/>
  <MarginWrapper bgClass="surface-800" v-if="showIndexPanel">
    <IndexPanel :stockIndexObject="stockIndexObject" @index-click="handleIndex"/>
  </MarginWrapper>
  <p>Auto: {{autocompletefocus}}</p>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import IndexPanel from "@/components/pages/indices/IndexPanel";
import {stockIndexObject} from "@/utils/indexMapper";

export default {
  name: "SearchTaxonomy",
  components: { IndexPanel, MarginWrapper, EuTaxoSearchBar},
  data(){
    return {
      autocompletefocus: null,
      showIndexPanel: true,
      index: null,
      stockIndexObject: stockIndexObject(),
      stockIndex: null
    }
  },
  methods: {
    getAutoCompleteFocus(focus){
      this.autocompletefocus = focus
      this.showIndexPanel = false
    },
    handleIndex(stockIndex, index){
      console.log(index)
      this.showIndexPanel = false
      this.$refs.euTaxoSearchBar.toggleIndexTabs(index, stockIndex)
      this.index = index
      this.stockIndex = stockIndex
    }
  }
}
</script>