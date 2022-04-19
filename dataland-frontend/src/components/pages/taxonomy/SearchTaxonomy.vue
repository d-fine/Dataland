<template>
  <SearchTaxonomyHeader :scrolled="scrolled"/>
  <EuTaxoSearchBar @autocomplete-focus="getAutoCompleteFocus" :stockIndexObject="stockIndexObject" ref="euTaxoSearchBar" @scrolling="handleScrolling"/>
  <IndexPanel :stockIndexObject="stockIndexObject" @index-click="handleIndex" :showIndexPanel="showIndexPanel"/>
  <pre>
    {{stockIndexObject}}
  </pre>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
  <br>
    <pre>
    {{stockObject}}

    </pre>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import IndexPanel from "@/components/pages/indices/IndexPanel";
import {stockIndexObject} from "@/utils/indexMapper";
import {StringHumanizer} from "@/utils/StringHumanizer"
const stringHumanizer = new StringHumanizer()
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
const stockIndexKeys = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum
const stockObject = stockIndexKeys.reduce((a, v) => ({ ...a, [v]: stringHumanizer.humanize(v)}), {})
import SearchTaxonomyHeader from "@/components/pages/taxonomy/SearchTaxonomyHeader";

export default {
  name: "SearchTaxonomy",
  components: {SearchTaxonomyHeader, IndexPanel, EuTaxoSearchBar},
  data(){
    return {
      showIndexPanel: true,
      scrolled: false,
      stockIndexObject: stockIndexObject(),
      stockIndexKeys: stockIndexKeys,
      stockObject: stockObject,
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