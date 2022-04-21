<template>
  <SearchTaxonomyHeader :scrolled="scrolled"/>
  <EuTaxoSearchBar @autocomplete-focus="handleAutoCompleteFocus" :stockIndices="stockIndices" ref="euTaxoSearchBar" @scrolling="handleScrolling"/>
  <IndexPanel @index-click="handleIndex" :showIndexPanel="showIndexPanel" :stockIndices="stockIndices"/>
</template>
<script>

import SearchTaxonomyHeader from "@/components/resources/taxonomy/search/SearchTaxonomyHeader";
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import IndexPanel from "@/components/resources/indices/IndexPanel";
import {useRoute} from "vue-router"
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";


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
      this.$refs.euTaxoSearchBar.toggleIndexTabs(stockIndex, index)
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