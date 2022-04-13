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
    <EuTaxoSearchBar @autocomplete-focus="getAutoCompleteFocus" :indexArray="indexArray" ref="euTaxoSearchBar"/>
  <MarginWrapper bgClass="surface-800" v-if="showIndexPanel">
    <IndexPanel :indexArray="indexArray" @index-click="handleIndex"/>
  </MarginWrapper>
  <p>Auto: {{autocompletefocus}}</p>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import IndexPanel from "@/components/pages/indices/IndexPanel";

const indexArray = [
  "CDAX",
  "DAX",
  "General Standards",
  "GEX",
  "MDAX",
  "Prime Standards",
  "SDAX",
  "TecDAX",
  "ScaleHDAX",
  "DAX 50 ESG"
]
export default {
  name: "SearchTaxonomy",
  components: { IndexPanel, MarginWrapper, EuTaxoSearchBar},
  data(){
    return {
      autocompletefocus: null,
      showIndexPanel: true,
      indexArray: indexArray,
      index: null
    }
  },
  methods: {
    getAutoCompleteFocus(focus){
      this.autocompletefocus = focus
      this.showIndexPanel = false
    },
    handleIndex(index){
      console.log(index)
      this.showIndexPanel = false
      this.$refs.euTaxoSearchBar.toggleIndexTabs(index)
      this.index = index
    }
  }
}
</script>