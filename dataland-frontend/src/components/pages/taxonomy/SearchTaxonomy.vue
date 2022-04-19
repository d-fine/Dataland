<template>
  <SearchTaxonomyHeader :scrolled="scrolled"/>
  <EuTaxoSearchBar @autocomplete-focus="handleAutoCompleteFocus" :stockIndexObject="stockIndexObject" ref="euTaxoSearchBar" @scrolling="handleScrolling" @query-action="handleQuery"/>
  <IndexPanel :stockIndexObject="stockIndexObject" @index-click="handleIndex" :showIndexPanel="showIndexPanel"/>
<p>
IndexPanel: {{showIndexPanel}}
</p>
</template>
<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import IndexPanel from "@/components/pages/indices/IndexPanel";
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
import {StringHumanizer} from "@/utils/StringHumanizer"
const stringHumanizer = new StringHumanizer()
const stockIndexObject = apiSpecs.components.schemas.CompanyInformation.properties["indices"]
                        .items.enum.reduce((a, v) => ({ ...a, [v]: stringHumanizer.humanize(v)}), {})
import SearchTaxonomyHeader from "@/components/pages/taxonomy/SearchTaxonomyHeader";

export default {
  name: "SearchTaxonomy",
  components: {SearchTaxonomyHeader, IndexPanel, EuTaxoSearchBar},
  data(){
    return {
      showIndexPanel: true,
      scrolled: false,
      stockIndexObject: stockIndexObject,
      stockIndex: null
    }
  },
  methods: {
    handleAutoCompleteFocus(){
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
    },
    handleQuery(){
      this.showIndexPanel = false
    }
  }
}
</script>