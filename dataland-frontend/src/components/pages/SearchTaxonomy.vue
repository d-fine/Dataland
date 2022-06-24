<template>
  <AuthenticationWrapper>
    <TheHeader/>
    <TheContent>
      <SearchTaxonomyHeader :scrolled="scrolled"/>
      <EuTaxoSearchBar @autocomplete-focus="handleAutoCompleteFocus" :stockIndices="stockIndices" ref="euTaxoSearchBar"
                       @scrolling="handleScrolling"/>
    </TheContent>
  </AuthenticationWrapper>
</template>
<script>

import SearchTaxonomyHeader from "@/components/resources/taxonomy/search/SearchTaxonomyHeader";
import EuTaxoSearchBar from "@/components/resources/taxonomy/search/EuTaxoSearchBar";
import TheHeader from "@/components/structure/TheHeader"
import TheContent from "@/components/structure/TheContent"
import {useRoute} from "vue-router"
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper";


export default {
  name: "SearchTaxonomy",
  components: {
    SearchTaxonomyHeader,
    EuTaxoSearchBar,
    TheHeader,
    TheContent,
    AuthenticationWrapper
  },
  data() {
    return {
      showIndexPanel: true,
      scrolled: false,
      stockIndices: apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum,
      route: useRoute(),
    }
  },
  methods: {
    handleAutoCompleteFocus() {
      this.showIndexPanel = false
      if (this.$refs.euTaxoSearchBar) {
        this.$refs.euTaxoSearchBar.showIndexTabs = true
      }
    },
    handleIndex(stockIndex, index) {
      this.showIndexPanel = false
      this.$refs.euTaxoSearchBar.toggleIndexTabs(stockIndex, index)
    },
    handleScrolling(scrolled) {
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