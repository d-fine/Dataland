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
    <EuTaxoSearchBar @autocomplete-focus="getAutoCompleteFocus"/>
  <MarginWrapper>
    <IndexTabs/>

  </MarginWrapper>
  <MarginWrapper bgClass="surface-800">
    <IndexPanel/>
  </MarginWrapper>
  <p>Auto: {{autocompletefocus}}</p>
</template>

<script>

import EuTaxoSearchBar from "@/components/ui/EuTaxoSearchBar";
import MarginWrapper from "@/components/wrapper/MarginWrapper";
import IndexPanel from "@/components/pages/indices/IndexPanel";
import IndexTabs from "@/components/pages/indices/IndexTabs";

export default {
  name: "SearchTaxonomy",
  components: {IndexTabs, IndexPanel, MarginWrapper, EuTaxoSearchBar},
  data(){
    return {
      autocompletefocus: null
    }
  },
  methods: {
    getAutoCompleteFocus(focus){
      this.autocompletefocus = focus
    }
  }
}
</script>