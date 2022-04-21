<template>
  <SearchTaxonomyHeader :scrolled="scrolled"/>
  <EuTaxoSearchBar @autocomplete-focus="handleAutoCompleteFocus" :stockIndices="stockIndices" ref="euTaxoSearchBar" @scrolling="handleScrolling"/>
  <IndexPanel @index-click="handleIndex" :showIndexPanel="showIndexPanel" :stockIndices="stockIndices"/>
</template>
<script>

import SearchTaxonomyHeader from "@/components/pages/taxonomy/SearchTaxonomyHeader";
import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import IndexPanel from "@/components/pages/indices/IndexPanel";
import {useRoute} from "vue-router"
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";

export default {
  name: "SearchTaxonomy",
  components: {SearchTaxonomyHeader, IndexPanel, EuTaxoSearchBar},
  data(){
    return {
      showIndexPanel: true,
      scrolled: false,
      stockIndices: apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum,
      route: useRoute(),
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
    },
    handleScrolling(scrolled){
      this.scrolled = scrolled
    },
  },
  beforeMount() {
    if (this.route.query.input) {
      this.showIndexPanel = false
    }
  },
}
</script>