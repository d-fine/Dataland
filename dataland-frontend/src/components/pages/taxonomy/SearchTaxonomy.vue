<template>
  <SearchTaxonomyHeader :scrolled="scrolled"/>
  <EuTaxoSearchBar @autocomplete-focus="getAutoCompleteFocus" :stockIndexObject="stockIndexObject" ref="euTaxoSearchBar" @scrolling="handleScrolling"/>
  <IndexPanel :stockIndexObject="stockIndexObject" @index-click="handleIndex" :showIndexPanel="showIndexPanel"/>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import IndexPanel from "@/components/pages/indices/IndexPanel";
import {stockIndexObject} from "@/utils/indexMapper";
import SearchTaxonomyHeader from "@/components/pages/taxonomy/SearchTaxonomyHeader";

export default {
  name: "SearchTaxonomy",
  components: {SearchTaxonomyHeader, IndexPanel, EuTaxoSearchBar},
  data(){
    return {
      showIndexPanel: true,
      scrolled: false,
      stockIndexObject: stockIndexObject(),
      stockIndex: null
    }
  },
  methods: {
    getAutoCompleteFocus(){
      this.showIndexPanel = false
      this.$refs.euTaxoSearchBar.showIndexTabs = true

    },
    handleIndex(stockIndex, index){
      this.showIndexPanel = false
      this.$refs.euTaxoSearchBar.toggleIndexTabs(index, stockIndex)
      this.stockIndex = stockIndex
    },
    handleScrolling(scrolled){
      this.scrolled = scrolled
    }
  }
}
</script>